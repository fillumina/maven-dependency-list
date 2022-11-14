package com.fillumina.maven.reverse.dependency;

import com.fillumina.maven.reverse.dependency.XmlParser.XmlTag;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class XmlParserTest {

    @Test
    public void shouldGetSingleTag() {
        String xml = "<one/>";
        XmlParser parser = new XmlParser(xml);
        List<XmlTag> list = parser.getTree();
        assertEquals(1, list.size());
        XmlTag tag = list.get(0);
        assertTrue(tag.isSingle());
        assertEquals(0, tag.getStart());
        assertEquals(xml.length(), tag.getEnd());
    }

    @Test
    public void shouldGetOneTag() {
        String xml = "<one></one>";
        XmlParser parser = new XmlParser(xml);
        List<XmlTag> list = parser.getTree();
        assertEquals(1, list.size());
        XmlTag tag = list.get(0);
        assertFalse(tag.isSingle());
        assertEquals(0, tag.getStart());
        assertEquals(xml.length(), tag.getEnd());
    }

    @Test
    public void shouldGetTwoTags() {
        final String xml1 = "<one></one>";
        final String xml2 = "<two></two>";
        final String xml = xml1 + xml2;

        XmlParser parser = new XmlParser(xml);
        List<XmlTag> list = parser.getTree();
        assertEquals(2, list.size());

        XmlTag one = list.get(0);
        assertEquals("one", one.getName());
        assertEquals(0, one.getStart());
        assertEquals(xml1.length(), one.getEnd());

        XmlTag two = list.get(1);
        assertEquals("two", two.getName());
        assertEquals(xml1.length(), two.getStart());
        assertEquals(xml.length(), two.getEnd());
    }

    @Test
    public void shouldGetContentTag() {
        String xml = "<one>content</one>";
        XmlParser parser = new XmlParser(xml);
        List<XmlTag> list = parser.getTree();
        assertEquals(1, list.size());

        XmlTag one = list.get(0);
        assertEquals("one", one.getName());
        assertEquals("content", one.getContent());
        assertEquals(0, one.getStart());
        assertEquals(xml.length(), one.getEnd());
    }

    @Test
    public void shouldGetTwoContentTag() {
        final String xml1 = "<one>content one</one>";
        final String xml2 = "<two>content two</two>";
        String xml = xml1 + xml2;
        XmlParser parser = new XmlParser(xml);
        List<XmlTag> list = parser.getTree();
        assertEquals(2, list.size());

        XmlTag one = list.get(0);
        assertEquals("one", one.getName());
        assertEquals("content one", one.getContent());
        assertEquals(0, one.getStart());
        assertEquals(xml1.length(), one.getEnd());

        XmlTag two = list.get(1);
        assertEquals("two", two.getName());
        assertEquals("content two", two.getContent());
        assertEquals(xml1.length(), two.getStart());
        assertEquals(xml.length(), two.getEnd());
    }

    @Test
    public void shouldIgnoreNestedTag() {
        final String xml1 = "<one>content one</one>";
        final String xml3 = "<three><two>content two</two></three>";
        String xml = xml1 + xml3;
        XmlParser parser = new XmlParser(xml);
        List<XmlTag> list = parser.getTree();
        assertEquals(2, list.size());

        XmlTag one = list.get(0);
        assertEquals("one", one.getName());
        assertEquals("content one", one.getContent());
        assertEquals(0, one.getStart());
        assertEquals(xml1.length(), one.getEnd());

        XmlTag three = list.get(1);
        assertEquals("three", three.getName());
        assertEquals(xml1.length(), three.getStart());
        assertEquals(xml.length(), three.getEnd());
        assertEquals(1, three.getChildren().size());
    }
}
