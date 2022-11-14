package com.fillumina.maven.reverse.dependency;

import com.fillumina.maven.reverse.dependency.XmlParser.XmlTag;
import java.util.List;

/**
 * Needed to search tags through a hierarchy.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class XmlTagFinder {

    private int startIndex;
    private final List<XmlTag> rootList;

    public XmlTagFinder(CharSequence buf, int startIndex) {
        this.startIndex = startIndex;
        rootList = new XmlParser(buf).getTree();
    }

    public int indexOf(String name, String content) {
        for (XmlTag tag : rootList) {
            if (name.equals(tag.getName()) && content.equals(tag.getContent())) {
                return startIndex + tag.getStart();
            }
        }
        return -1;
    }

}
