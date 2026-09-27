package com.fillumina.maven.reverse.dependency;

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

    private ParentPom() {
    }

    /**
     * Returns the properties visible from {@code pom}: the ones it declares itself,
     * which win, over the ones declared by each ancestor reached through
     * {@code <relativePath>}. A property that cannot be read is simply absent, and
     * the caller is left to say so.
     */
    static Map<String, String> inheritedProperties(Path pom, Map<String, String> ownProperties) {
        Map<String, String> inherited = new HashMap<>();
        Set<Path> visited = new HashSet<>();
        Path current = absolute(pom);
        for (int depth = 0; current != null && depth < MAX_DEPTH; depth++) {
            if (!visited.add(current) || !Files.isReadable(current)) {
                break;
            }
            Document document = parse(current);
            if (document == null) {
                break;
            }
            readProperties(document, inherited);
            current = parentOf(current, document);
        }
        inherited.putAll(ownProperties);
        return inherited;
    }

    private static Path parentOf(Path pom, Document document) {
        Element parent = firstElement(document, "parent");
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
            // an empty <relativePath/> means the parent is not on the filesystem
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
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            try (InputStream is = Files.newInputStream(pom)) {
                Document document = builder.parse(is);
                document.getDocumentElement().normalize();
                return document;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
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
