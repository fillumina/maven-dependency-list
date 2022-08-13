package com.fillumina.maven.reverse.dependency;

import com.fillumina.maven.reverse.dependency.PackageId;
import java.io.File;
import java.io.IOException;
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
public class PomLoader {
    public static final PomLoader INSTANCE = new PomLoader();

    private PomLoader() {}

    public void loader(File pomFile, AssociationBuilder associationBuilder) {
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(pomFile);

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            PackageId name = extractProject(doc.getDocumentElement());
            parseDependency(doc, name, associationBuilder);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void parseDependency(Document doc, PackageId name, AssociationBuilder associationBuilder)
            throws DOMException, NumberFormatException {
        NodeList list = doc.getElementsByTagName("dependency");

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                PackageId dependency = extractProject(element);
                associationBuilder.add(name, dependency);
            }
        }
    }

    private PackageId extractProject(Element element) throws DOMException {
        String groupId = extractTagText(element, "groupId");
        String artifactId = extractTagText(element, "artifactId");
        String version = extractTagText(element, "version");
        PackageId dependency = new PackageId(groupId, artifactId, version);
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
        return element.getElementsByTagName(tagName).item(0);
    }


}