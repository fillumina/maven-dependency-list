package com.fillumina.maven.reverse.dependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the properties a pom inherits from the poms above it, following
 * {@code <relativePath>} on disk. That is the whole of this tool's idea of
 * inheritance: no local repository, no imported BOM, no profile, and nothing
 * that is not reachable by walking up the directory tree.
 */
final class ParentPom {

    /**
     * Bounds the walk, so a {@code <parent>} cycle in a broken tree cannot spin
     * forever. A visited set stops cycles too; this bounds a merely deep chain.
     */
    static final int MAX_DEPTH = 16;

    /**
     * A tree of projects walks over the same few parents over and over, and
     * parsing is the expensive part, so each file is parsed once. The tool is
     * single threaded and lives for one run, so the map is never evicted and
     * never guarded.
     */
    private static final Map<Path, Document> PARSED = new HashMap<>();

    private ParentPom() {
    }

    /**
     * Returns the properties visible from {@code pom}: the ones it declares
     * itself, which win, over the ones declared by each ancestor reached through
     * {@code <relativePath>}, and over those of the parent it declares when that
     * parent is not on disk and a {@code repository} was given to look in. A parent
     * that is neither on disk nor in the repository contributes nothing and the
     * caller is left to say so.
     */
    static Map<String, String> inheritedProperties(Path pom, Map<String, String> ownProperties,
            Path repository) {
        Map<String, String> inherited = new HashMap<>();
        Set<Path> visited = new HashSet<>();
        Element declaredParent = null;
        boolean parentOnDisk = false;
        Path current = absolute(pom);
        for (int depth = 0; current != null && depth < MAX_DEPTH; depth++) {
            if (!visited.add(current) || !Files.isReadable(current)) {
                break;
            }
            Document document = parse(current);
            if (document == null) {
                break;
            }
            if (depth > 0) {
                if (!isTheDeclaredParent(declaredParent, document)) {
                    // a file that is not the parent the child named must not be
                    // allowed to lend it properties
                    break;
                }
                parentOnDisk = true;
            }
            readProperties(document, inherited);
            declaredParent = firstElement(document, "parent");
            current = parentPath(current, declaredParent);
        }
        if (repository != null && !parentOnDisk) {
            // the parent the child named was not on disk, which is what an empty
            // <relativePath/> asks for, so a repository is where it should be
            readFromRepository(declaredParent, repository, inherited, new HashSet<>());
        }
        inherited.putAll(ownProperties);
        return inherited;
    }

    /**
     * Reads the properties of a parent out of a local Maven repository, and of that
     * parent's own parent, and so on. The path a repository gives is built from the
     * coordinates, so there is nothing to check it against: either the file named by
     * those coordinates is there or it is not.
     */
    private static void readFromRepository(Element declared, Path repository, Map<String, String> target,
            Set<Path> visited) {
        Path pom = repositoryPath(declared, repository);
        if (pom == null || !visited.add(pom) || !Files.isReadable(pom)) {
            return;
        }
        Document document = parse(pom);
        if (document == null) {
            return;
        }
        readProperties(document, target);
        readFromRepository(firstElement(document, "parent"), repository, target, visited);
    }

    /**
     * Where a repository keeps the pom of the parent a child declared, which is
     * {@code groupId/with/slashes/artifactId/version/artifactId-version.pom}. A
     * version that is a property gives no such path, and then there is nothing to
     * look up.
     */
    private static Path repositoryPath(Element declared, Path repository) {
        if (declared == null) {
            return null;
        }
        String groupId = text(declared, "groupId");
        String artifactId = text(declared, "artifactId");
        String version = text(declared, "version");
        if (!isLiteral(groupId) || !isLiteral(artifactId) || !isLiteral(version)) {
            return null;
        }
        String file = artifactId.trim() + "-" + version.trim() + ".pom";
        return absolute(repository.resolve(groupId.trim().replace('.', File.separatorChar))
                .resolve(artifactId.trim())
                .resolve(version.trim())
                .resolve(file));
    }

    private static boolean isLiteral(String value) {
        return value != null && !value.isBlank() && !value.trim().startsWith("${");
    }

    /**
     * Whether the file just opened really is the parent the child declared, by
     * the coordinates the child gave. A version either side of which is a
     * property is not checked, because it cannot be without resolving first, and
     * refusing on that would be worse than trusting it.
     */
    private static boolean isTheDeclaredParent(Element declared, Document candidate) {
        if (declared == null) {
            return false;
        }
        Element root = candidate.getDocumentElement();
        return agrees(text(declared, "groupId"), effective(root, "groupId"))
                && agrees(text(declared, "artifactId"), text(root, "artifactId"))
                && agrees(text(declared, "version"), effective(root, "version"));
    }

    /**
     * The groupId or version a pom ends up with, which is not always the one it
     * writes: a child that inherits either from its parent writes neither, so the
     * chain of {@code <parent>} blocks is followed until one of them does. This
     * reads text only and never touches the filesystem, and the depth is bounded
     * so a cycle of {@code <parent>} elements cannot spin.
     */
    private static String effective(Element project, String tagName) {
        Element current = project;
        Element parent = (Element) extractTag(current, "parent");
        for (int depth = 0; current != null && depth < MAX_DEPTH; depth++) {
            Element declared = (Element) extractTag(current, tagName);
            if (declared != null) {
                return declared.getTextContent();
            }
            Element next = parent == null ? null : (Element) extractTag(parent, "parent");
            current = parent;
            parent = next;
        }
        return null;
    }

    private static boolean agrees(String declared, String found) {
        if (declared == null || declared.isEmpty()) {
            return true;
        }
        if (found == null || found.isEmpty()) {
            return false;
        }
        if (isProperty(declared) || isProperty(found)) {
            return true;
        }
        return declared.trim().equals(found.trim());
    }

    private static boolean isProperty(String value) {
        return value.startsWith("${") && value.endsWith("}");
    }

    /**
     * Where {@code pom} says its parent is. A parent with no
     * {@code <relativePath>} sits at {@code ../pom.xml}, and one with an empty
     * {@code <relativePath/>} is not on the filesystem at all.
     */
    private static Path parentPath(Path pom, Element parent) {
        if (parent == null) {
            return null;
        }
        Path folder = pom.getParent();
        if (folder == null) {
            return null;
        }
        Element declared = (Element) extractTag(parent, "relativePath");
        if (declared != null) {
            String relativePath = declared.getTextContent().trim();
            return relativePath.isEmpty() ? null : absolute(folder.resolve(relativePath));
        }
        return absolute(folder.resolve("../pom.xml"));
    }

    private static void readProperties(Document document, Map<String, String> target) {
        NodeList properties = document.getElementsByTagName("properties");
        if (properties.getLength() == 0) {
            return;
        }
        NodeList children = properties.item(0).getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                // the nearest ancestor wins, so an already seen property is kept
                target.putIfAbsent(child.getNodeName(), child.getTextContent());
            }
        }
    }

    private static Document parse(Path pom) {
        Document cached = PARSED.get(pom);
        if (cached != null) {
            return cached;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;
            try (InputStream is = Files.newInputStream(pom)) {
                document = builder.parse(is);
            }
            document.getDocumentElement().normalize();
            PARSED.put(pom, document);
            return document;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
    }

    /**
     * The text of a direct child of {@code element}, and null when there is none.
     * Direct, because a pom's own {@code <groupId>} has to be told apart from the
     * one inside its {@code <parent>}.
     */
    private static String text(Element element, String tagName) {
        Node node = extractTag(element, tagName);
        return node == null ? null : node.getTextContent();
    }

    private static Element firstElement(Document document, String tagName) {
        NodeList nodes = document.getDocumentElement().getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    private static Node extractTag(Element element, String tagName) {
        NodeList children = element.getChildNodes();
        for (int i = 0, l = children.getLength(); i < l; i++) {
            Node node = children.item(i);
            if (tagName.equals(node.getNodeName())) {
                return node;
            }
        }
        return null;
    }

    private static Path absolute(Path path) {
        return path.toAbsolutePath().normalize();
    }
}
