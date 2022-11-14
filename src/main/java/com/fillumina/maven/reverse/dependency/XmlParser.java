package com.fillumina.maven.reverse.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses XML elements in a hierarchy.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class XmlParser {

    public static class XmlTag {
        private XmlTag parent;
        private final String name;
        private final int start;
        private int end;
        private final boolean closing;
        private final boolean single;
        private String content;

        private final List<XmlTag> children = new ArrayList<>();

        public XmlTag(String name, int start, int end, boolean closing, boolean single) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.closing = closing;
            this.single = single;
        }

        public List<XmlTag> getChildren() {
            return children;
        }

        public XmlTag getParent() {
            return parent;
        }

        public String getName() {
            return name;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public boolean isClosing() {
            return closing;
        }

        public boolean isSingle() {
            return single;
        }

        public boolean isRoot() {
            return parent == null;
        }

        private void addChild(XmlTag tag) {
            tag.parent = this;
            children.add(tag);
        }

        private void setEnd(int end) {
            this.end = end;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return name + (content != null ? ": " + content : "");
        }
    }

    private static final Pattern COMMENT = Pattern.compile("(<!--.*-->)");
    private static final Pattern QUOTE = Pattern.compile("(\".*\")");

    private List<XmlTag> tree;

    public XmlParser(CharSequence buf) {
        Map<Integer,Integer> jumpMap = new HashMap<>();
        createCommentJumpMap(buf, jumpMap, COMMENT);
        createCommentJumpMap(buf, jumpMap, QUOTE);

        List<XmlTag> list = createTagList(buf, jumpMap);
        addChildren(list, buf);
        tree = list.stream()
                .filter(t -> t.isRoot() && !t.isClosing())
                .toList();
    }

    public List<XmlTag> getTree() {
        return tree;
    }

    private void addChildren(List<XmlTag> list, CharSequence buf) {
        for (int i=0,l=list.size(); i<l; i++) {
            XmlTag xtag = list.get(i);
            if (xtag.isClosing()) {
                int startIndex = findBackwardTagWithName(list, i, xtag.getName());
                XmlTag startTag = list.get(startIndex);
                if (startIndex == i - 1) {
                    String content = buf.subSequence(startTag.getEnd(), xtag.start).toString();
                    if (!"".equals(content)) {
                        startTag.setContent(content);
                    }
                } else {
                    for (int j=startIndex + 1; j<i; j++) {
                        XmlTag child = list.get(j);
                        if (!child.isClosing()) {
                            startTag.addChild(child);
                        }
                    }
                }
                startTag.setEnd(xtag.end);
            }
        }
    }

    static int findBackwardTagWithName(List<XmlTag> list, int position, String name) {
        for (int i=position-1; i>=0; i--) {
            XmlTag xtag = list.get(i);
            if (name.equals(xtag.getName())) {
                return i;
            }
        }
        return -1;
    }

    static List<XmlTag> createTagList(CharSequence buf, Map<Integer, Integer> jumpMap) {
        List<XmlTag> list = new ArrayList<>();
        int length = buf.length();
        int start = -1;
        char prev = 0;
        boolean closing = false;
        for (int i=0; i<length; i++) {
            Integer jump = jumpMap.get(i);
            if (jump != null) {
                i = jump;
            }

            char c = buf.charAt(i);
            if (c == '<') {
                start = i;
            } else if (c == '/' && prev == '<') {
                closing = true;
            } else if (c == '>') {
                String name = buf.subSequence(start, i).toString().replaceAll("[</>]", "");
                XmlTag xtag = new XmlTag(name, start, i+1, closing, prev == '/');
                list.add(xtag);
                closing = false;
            }
            prev = c;
        }
        return list;
    }

    private void createCommentJumpMap(CharSequence buf, Map<Integer,Integer> map, Pattern pattern) {
        Matcher matcher = pattern.matcher(buf);
        while (matcher.find()) {
            matcher.start(1);
            map.put(matcher.start(1), matcher.end(1));
        }
    }

}
