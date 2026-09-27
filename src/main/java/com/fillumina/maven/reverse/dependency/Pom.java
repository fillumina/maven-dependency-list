package com.fillumina.maven.reverse.dependency;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class Pom {

    private final PackageId pomPackage;
    private final Map<String,String> propertyMap;
    private final Map<String,String> ownPropertyMap;
    private final Path source;
    private final List<String> unresolvedWarnings = new ArrayList<>();

    public Pom(String pom, AssociationBuilder associationBuilder, boolean noDependencies) {
        this(pom, null, associationBuilder, noDependencies);
    }

    /**
     * Reads one pom. A version written as a property is resolved against the
     * properties of the poms above {@code source} on disk, and keeps its
     * placeholder when it cannot be resolved.
     *
     * @param source where the text was read from, used in warnings and to find the
     *               parents, and null only when the text did not come from a file
     */
    public Pom(String pom, Path source, AssociationBuilder associationBuilder,
            boolean noDependencies) {
        this.source = source;
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputStream is = new ByteArrayInputStream(pom.getBytes(StandardCharsets.UTF_8));
            Document doc = db.parse(is);

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            ownPropertyMap = parseProperties(doc);
            propertyMap = source == null
                    ? ownPropertyMap
                    : ParentPom.inheritedProperties(source, ownPropertyMap);

            pomPackage = parsePomPackage(doc);
            seedParentCoordinates(doc);

            if (noDependencies) {
                associationBuilder.add(pomPackage, null);
            } else {
                parseDependency(doc, propertyMap, pomPackage, "dependency", associationBuilder);
                parseDependency(doc, propertyMap, pomPackage, "plugin", associationBuilder);
            }
            unresolvedWarnings.forEach(warning -> System.err.println("WARNING: " + warning));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Puts the parent pom's own groupId and version in the properties this pom can
     * be read with, under both the short spelling `${parent.version}` and the dotted
     * one `${project.parent.version}`, which real poms use for both. They are not
     * properties the file declares, so they go in the visible map only and a rewrite
     * never mistakes one for something it may edit here. A parent block whose own
     * version is a property contributes nothing, because resolving that would need the
     * map this is being added to.
     */
    private void seedParentCoordinates(Document doc) throws DOMException {
        NodeList parents = doc.getDocumentElement().getElementsByTagName("parent");
        if (parents.getLength() != 1) {
            return;
        }
        Element parent = (Element) parents.item(0);
        seed("groupId", extractTagText(parent, "groupId"));
        seed("version", extractTagText(parent, "version"));
    }

    private void seed(String name, String value) {
        if (isLiteral(value)) {
            propertyMap.put("parent." + name, value);
            propertyMap.put("project.parent." + name, value);
        }
    }

    private static boolean isLiteral(String value) {
        return value != null && !value.isBlank() && !value.trim().startsWith("${");
    }

    private PackageId parsePomPackage(Document doc) throws DOMException {
        PackageId pom = extractProject(doc.getDocumentElement(), propertyMap, "");
        NodeList nodes = doc.getDocumentElement().getElementsByTagName("parent");
        if (nodes.getLength() == 1) {
            Element parentElement = (Element) nodes.item(0);
            if (parentElement != null) {
                PackageId parent = extractProject(parentElement, propertyMap, "parent");
                if (parent != null) {
                    if ( (pom.getGroupId() == null && parent.getGroupId() != null) ||
                            (pom.getVersion() == null && parent.getVersion() != null)) {
                        pom = new PackageId(
                                notNull(pom.getGroupId(), parent.getGroupId()),
                                pom.getArtifactId(),
                                notNull(pom.getVersion(), parent.getVersion())
                        );
                    }
                }
            }
        }
        return pom;
    }

    /**
     * The properties this pom declares itself, and the only ones a rewrite of this
     * file can change.
     */
    public Map<String, String> getOwnPropertyMap() {
        return ownPropertyMap;
    }

    public PackageId getPomPackage() {
        return pomPackage;
    }

    /**
     * Every property visible from this pom, its own plus those it inherits, which
     * is what says which version a dependency is actually on.
     */
    public Map<String, String> getPropertyMap() {
        return propertyMap;
    }

    private Map<String,String> parseProperties(Document doc)
            throws DOMException, NumberFormatException {
        final NodeList nodes = doc.getElementsByTagName("properties");
        if (nodes.getLength() == 0) {
            return Map.of();
        }
        Node properties = nodes.item(0);
        NodeList list = properties.getChildNodes();

        Map<String,String> map = new HashMap<>();

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String name = node.getNodeName();
                String value = node.getTextContent();
                map.put(name, value);
            }
        }

        return map;
    }

    private void parseDependency(Document doc, Map<String,String> versionMap,
            PackageId pkgName, String tagName, AssociationBuilder associationBuilder)
            throws DOMException, NumberFormatException {
        NodeList list = doc.getElementsByTagName(tagName);

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                PackageId dependency = extractProject(element, versionMap, tagName);
                associationBuilder.add(pkgName, dependency);
            }
        }
    }

    private PackageId extractProject(Element element, Map<String,String> versionMap, String tagName)
            throws DOMException {
        boolean isPlugin = "plugin".equals(tagName);
        String groupId = extractTagText(element, "groupId");
        String artifactId = extractTagText(element, "artifactId");
        String version = extractTagText(element, "version");
        String unresolvedProperty = null;
        if (version != null && version.startsWith("${")) {
            final String property = version.substring(2, version.length() - 1);
            final String resolved = versionMap.get(property);
            if (resolved == null) {
                // keep the placeholder: an unresolvable version is not the version
                // of the project, and saying so is better than guessing
                unresolvedProperty = property;
            } else {
                version = resolved;
            }
        }
        if (pomPackage != null) {
            if (groupId == null) {
                if (isPlugin) {
                    groupId = "org.apache.maven.plugins";
                } else {
                    groupId = pomPackage.getGroupId();
                }
            } else if (groupId.trim().equals("${project.groupId}")) {
                groupId = pomPackage.getGroupId();
            } else if (groupId.trim().startsWith("${")) {
                final String name = groupId.trim().substring(2, groupId.trim().length() - 1);
                final String resolved = versionMap.get(name);
                if (resolved != null) {
                    groupId = resolved;
                }
            }
            if (version == null || version.trim().equals("${project.version}")) {
                version = pomPackage.getVersion();
            }
        }
        // default groupId is 'org.apache.maven.pugins' if omitted
        // https://stackoverflow.com/questions/65527291/is-groupid-required-for-plugins-in-maven-pom-xml
        final String adjustedGroupId = groupId == null && isPlugin ? "org.apache.maven.pugins" : groupId;
        PackageId dependency = new PackageId(adjustedGroupId, artifactId, version);
        if (unresolvedProperty != null && isWorthReporting(tagName, unresolvedProperty)) {
            unresolvedWarnings.add(unresolvedWarning(dependency, unresolvedProperty));
        }
        return dependency;
    }

    /**
     * A property the model defines by itself is not an unresolved one: `${project.version}`
     * and `${project.groupId}` are filled in from the project being read, and the
     * project's own tags and its `<parent>` are not a dependency at all.
     */
    private static boolean isWorthReporting(String tagName, String property) {
        boolean isDependencyOrPlugin = "dependency".equals(tagName) || "plugin".equals(tagName);
        boolean isModelProperty = "project.version".equals(property) || "project.groupId".equals(property);
        return isDependencyOrPlugin && !isModelProperty;
    }

    private String unresolvedWarning(PackageId dependency, String property) {
        StringBuilder warning = new StringBuilder();
        if (source != null) {
            warning.append(source).append(": ");
        }
        return warning.append(dependency.getName())
                .append(" has version ${").append(property)
                .append("}, which is not defined in this pom.xml")
                .append(source == null ? "" : " or in any parent pom found on disk")
                .toString();
    }

    private String extractTagText(Element element, String tagName) throws DOMException {
        final Node node = extractTag(element, tagName);
        if (node == null) {
            return null;
        }
        return node.getTextContent();
    }

    private static Node extractTag(Element element, String tagName) {
        NodeList children = element.getChildNodes();
        for (int i=0,l=children.getLength(); i<l; i++) {
            Node node = children.item(i);
            String name = node.getNodeName();
            if (tagName.equals(name)) {
                return node;
            }
        }
        return null;
    }

    private static String notNull(String a, String b) {
        return a == null ? b : a;
    }
}
