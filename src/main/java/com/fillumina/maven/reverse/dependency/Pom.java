package com.fillumina.maven.reverse.dependency;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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

    public Pom(String pom, AssociationBuilder associationBuilder, boolean noDependencies) {
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputStream is = new ByteArrayInputStream(pom.getBytes());
            Document doc = db.parse(is);

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            propertyMap = parseProperties(doc);

            pomPackage = parsePomPackage(doc);

            if (noDependencies) {
                associationBuilder.add(pomPackage, null);
            } else {
                parseDependency(doc, propertyMap, pomPackage, "dependency", associationBuilder);
                parseDependency(doc, propertyMap, pomPackage, "plugin", associationBuilder);
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

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

    public PackageId getPomPackage() {
        return pomPackage;
    }

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
        if (version != null && version.startsWith("${")) {
            final String property = version.substring(2, version.length() - 1);
            version = versionMap.get(property);
        }
        if (pomPackage != null) {
            if (groupId == null || groupId.trim().equals("${project.groupId}")) {
                groupId = pomPackage.getGroupId();
            }
            if (version == null || version.trim().equals("${project.version}")) {
                version = pomPackage.getVersion();
            }
        }
        // default groupId is 'org.apache.maven.pugins' if omitted
        // https://stackoverflow.com/questions/65527291/is-groupid-required-for-plugins-in-maven-pom-xml
        final String adjustedGroupId = groupId == null && isPlugin ? "org.apache.maven.pugins" : groupId;
        PackageId dependency = new PackageId(adjustedGroupId, artifactId, version);
        return dependency;
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
