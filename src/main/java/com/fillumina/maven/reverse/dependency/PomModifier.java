package com.fillumina.maven.reverse.dependency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Changes the version of a dependency or a plugin in the original {@code pom.xml}. If the version
 * is specified as a property (in the same file) it changes the property value unless the same
 * property is shared with other dependencies or plugins, in which case it changes the version in
 * place. <i>It doesn't manage properties defined outside the same {@code pom.xml}</i>.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PomModifier {

    public static final PomModifier INSTANCE = new PomModifier();

    private PomModifier() {
    }

    public StringBuffer modify(String pom, Map<String, String> propertyMap,
            PackageId packageId, String newVersion) throws IOException {
        StringBuffer pomBuffer = new StringBuffer(pom);
        return modifyBuffer(pomBuffer, propertyMap, packageId, newVersion);
    }

    static StringBuffer modifyBuffer(StringBuffer pom, Map<String, String> propertyMap,
            PackageId packageId, String newVersion) {
        boolean modified = false;
        int idx = 0;
        while (true) {
            final String artifactId = createTag("artifactId", packageId.getArtifactId());
            idx = pom.indexOf(artifactId, idx);
            if (idx == -1) {
                break;
            }
            int lastIndexPlugin = lastIndexOf(pom.subSequence(0, idx), "<plugin>");
            int lastIndexDependency = lastIndexOf(pom.subSequence(0, idx), "<dependency>");
            boolean isDependency = true;
            int startIndex = lastIndexDependency;
            if (lastIndexPlugin > lastIndexDependency) {
                isDependency = false;
                startIndex = lastIndexPlugin;
            }
            startIndex += (isDependency ? "<dependency>" : "<plugin>").length();
            int endIndex = isDependency
                    ? indexOf(pom.subSequence(startIndex, pom.length()), "</dependency>")
                    : indexOf(pom.subSequence(startIndex, pom.length()), "</plugin>");
            CharSequence block = pom.subSequence(startIndex, startIndex + endIndex);
            XmlTagFinder finder = new XmlTagFinder(block, startIndex);
            final int groupOfIndex = finder.indexOf("groupId", packageId.getGroupId());
            if (groupOfIndex != -1) {
                String requiredVersion = packageId.getVersion();
                String versionContent = extractVersionContent(block);
                String actualVersion = substituteProperties(versionContent, propertyMap);
                if (actualVersion != null && actualVersion.equals(requiredVersion)) {
                    boolean isProperty = versionContent.matches("^\\$\\{(.*)\\}$") &&
                            countOccurrences(pom, versionContent) == 1;
                    if (isProperty) {
                        final String propertyName = versionContent.substring(2, versionContent
                                .length() - 1);
                        final String propertyContent = createTag(propertyName, actualVersion);
                        final String newPropertyContent = createTag(propertyName, newVersion);
                        final int propertyIndex = indexOf(pom, propertyContent);
                        if (propertyIndex != -1) {
                            pom.replace(propertyIndex, propertyIndex + propertyContent.length(),
                                    newPropertyContent);
                        }
                        modified = true;
                    } else {
                        final int versionIndex = finder.indexOf("version", versionContent);

                        if (versionIndex != -1) {
                            final String subsitutedVersion = createTag("version", newVersion);
                            final String oldVersion = createTag("version", versionContent);
                            pom.replace(versionIndex, versionIndex + oldVersion.length(),
                                    subsitutedVersion);
                            modified = true;
                        }
                    }
                }
            }
            idx += endIndex;
        }
        return modified ? pom : null;
    }

    static String createTag(final String name, String value) {
        return "<" + name + ">" + value + "</" + name + ">";
    }

    static String substituteProperties(String content, Map<String, String> propertyMap) {
        if (content == null || content.isBlank() || !content.contains("${")) {
            return content;
        }
        List<String> properties = extractProperty(content);
        String result = content;
        for (String property : properties) {
            String value = propertyMap.get(property);
            if (value != null) {
                result = result.replace("${" + property + "}", value);
            }
        }
        return result;
    }

    private static final Pattern VERSION_EXTRACTOR = Pattern.compile("<version>(.*)</version>");

    static String extractVersionContent(CharSequence cs) {
        final Matcher matcher = VERSION_EXTRACTOR.matcher(cs);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static final Pattern PROPERTY_EXTRACTOR = Pattern.compile("\\$\\{(.*)\\}");

    static List<String> extractProperty(CharSequence cs) {
        List<String> list = new ArrayList<>();
        Matcher matcher = PROPERTY_EXTRACTOR.matcher(cs);
        while (matcher.find()) {
            list.add(matcher.group(1));
        }
        return list;
    }

    /**
     * Counts how many occurrences of the given search string are in the sequence.
     */
    static int countOccurrences(CharSequence sequence, String search) {
        int counter = 0;
        int index = -1;
        while (true) {
            index = indexOf(sequence, search, index + 1);
            if (index == -1) {
                return counter;
            }
            counter++;
        }
    }

    /**
     * Searches for the first occurrence of the given string.
     */
    static int indexOf(CharSequence sequence, String search) {
        return indexOf(sequence, search, 0);
    }

    /**
     * Searches for the first occurrence of the given string starting from startIndex.
     */
    static int indexOf(CharSequence sequence, String search, int startIndex) {
        final int searchLength = search.length();
        final int sequenceLength = sequence.length();
        int searchIndex = 0;
        char searchCharacter = search.charAt(searchIndex);
        for (int idx = startIndex; idx < sequenceLength; idx++) {
            char sequenceCharacter = sequence.charAt(idx);
            if (sequenceCharacter == searchCharacter) {
                searchIndex++;
                if (searchIndex == searchLength) {
                    return idx - searchLength + 1;
                }
                searchCharacter = search.charAt(searchIndex);
            } else if (searchIndex != 0) {
                searchIndex = 0;
                searchCharacter = search.charAt(searchIndex);
                idx--;
            }
        }
        return -1;
    }

    /**
     * Searches backwards for the first occurrence of the given string.
     */
    static int lastIndexOf(CharSequence sequence, String search) {
        final int searchLength = search.length() - 1;
        int searchIndex = searchLength;
        char searchCharacter = search.charAt(searchIndex);
        for (int idx = sequence.length() - 1; idx >= 0; idx--) {
            char sequenceCharacter = sequence.charAt(idx);
            if (sequenceCharacter == searchCharacter) {
                searchIndex--;
                if (searchIndex < 0) {
                    return idx;
                }
                searchCharacter = search.charAt(searchIndex);
            } else if (searchIndex != searchLength) {
                searchIndex = searchLength;
                searchCharacter = search.charAt(searchIndex);
                idx++;
            }
        }
        return -1;
    }

}
