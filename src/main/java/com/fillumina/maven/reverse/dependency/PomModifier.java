package com.fillumina.maven.reverse.dependency;

import java.io.IOException;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PomModifier {

    public static final PomModifier INSTANCE = new PomModifier();

    private PomModifier() {
    }

    public StringBuffer modify(String pom, PackageId packageId, String newVersion) throws IOException {
        StringBuffer pomBuffer = new StringBuffer(pom);
        return modifyBuffer(pomBuffer, packageId, newVersion);
    }

    static StringBuffer modifyBuffer(StringBuffer pom, PackageId packageId, String newVersion) {
        boolean modified = false;
        int idx = 0;
        while (true) {
            final String artifactId = "<artifactId>" + packageId.getArtifactId() + "</artifactId>";
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
            int endIndex = isDependency
                    ? indexOf(pom.subSequence(startIndex, pom.length()), "</dependency>")
                    : indexOf(pom.subSequence(startIndex, pom.length()), "</plugin>");
            CharSequence block = pom.subSequence(startIndex, startIndex + endIndex);
            final String groupId = "<groupId>" + packageId.getGroupId() + "</groupId>";
            final int groupOfIndex = indexOf(block, groupId);
            if (groupOfIndex != -1) {
                final String oldVersion = "<version>" + packageId.getVersion() + "</version>";
                final int versionIndex = indexOf(block, oldVersion);
                if (versionIndex != -1) {
                    final String subsitutedVersion = "<version>" + newVersion + "</version>";
                    int pomVersionIndex = startIndex + versionIndex;
                    pom.replace(pomVersionIndex, pomVersionIndex + oldVersion.length(), subsitutedVersion);
                    modified = true;
                }
            }
            idx += endIndex;
        }
        return modified ? pom : null;
    }

    /**
     * Search for the first occurrence of the given string.
     */
    static int indexOf(CharSequence sequence, String search) {
        final int searchLength = search.length();
        final int sequenceLength = sequence.length();
        int searchIndex = 0;
        char searchCharacter = search.charAt(searchIndex);
        for (int idx = 0; idx < sequenceLength; idx++) {
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
            }
        }
        return -1;
    }

    /**
     * Search backwards for the first occurrence of the given string.
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
            }
        }
        return -1;
    }

}
