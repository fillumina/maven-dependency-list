package com.fillumina.maven.reverse.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Covers what a version written as a property resolves to, with and without the
 * `-P` flag, and what the tool says when it resolves to nothing.
 */
public class PomInheritanceTest {

    private static final String PROPERTY_VERSION = "${lib.version}";
    private static final String NOT_DEFINED =
            "has version ${lib.version}, which is not defined in this pom.xml";

    @TempDir
    Path root;

    @Test
    public void shouldResolveAnInheritedVersionWithParents() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", null);

        assertResolved(run("-P"), "3.1");
    }

    @Test
    public void shouldKeepThePlaceholderWithoutParents() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", null);

        String[] output = run();

        assertUnresolved(output, NOT_DEFINED);
    }

    @Test
    public void shouldSayThatNoParentWasFoundOnDisk() throws IOException {
        writeChild("", PROPERTY_VERSION, "root", null);

        assertUnresolved(run("-P"), NOT_DEFINED + " or in any parent pom found on disk");
    }

    @Test
    public void shouldNotLookOnDiskWhenTheRelativePathIsEmpty() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", "");

        assertUnresolved(run("-P"), NOT_DEFINED + " or in any parent pom found on disk");
    }

    @Test
    public void shouldFollowACustomRelativePath() throws IOException {
        writeParent(properties("lib.version", "3.1"), "elsewhere", "elsewhere", null, null);
        writeChild("", PROPERTY_VERSION, "elsewhere", "../elsewhere/pom.xml");

        assertResolved(run("-P"), "3.1");
    }

    @Test
    public void shouldPreferThePropertyOfTheChildOverTheOneOfTheParent() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild(properties("lib.version", "9.9"), PROPERTY_VERSION, "root", null);

        assertResolved(run("-P"), "9.9");
    }

    @Test
    public void shouldResolveFromAGrandparent() throws IOException {
        writeParent(properties("lib.version", "3.1"), "elsewhere", "elsewhere");
        writeParent("", "middle", "middle", "elsewhere", "../elsewhere/pom.xml");
        writeChild("", PROPERTY_VERSION, "middle", "../middle/pom.xml");

        assertResolved(run("-P"), "3.1");
    }

    @Test
    public void shouldStopOnAParentCycle() throws IOException {
        writeParent(properties("lib.version", "3.1"), "elsewhere", "elsewhere", "child",
                "../child/pom.xml");
        writeChild("", PROPERTY_VERSION, "elsewhere", "../elsewhere/pom.xml");

        assertResolved(run("-P"), "3.1");
    }

    @Test
    public void shouldNotWarnAboutAPropertyTheModelDefines() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild("", "${project.version}", "root", null);

        String[] output = run("-P");

        assertEquals("", output[1], "stderr:" + output[1]);
        assertTrue(output[0].contains("org.acme:lib:5.0"), "stdout:" + output[0]);
    }

    @Test
    public void shouldPinAnInheritedVersionInTheChild() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", null);

        String[] output = CommandOutput.capture(() -> assertEquals(0, App.run(new String[]{
                "-P", "-c", "org.acme:lib:3.1:4.0", root.toString()})));

        String child = Files.readString(root.resolve("child/pom.xml"));
        assertTrue(child.contains("<version>4.0</version>"), child);
        assertFalse(child.contains(PROPERTY_VERSION), child);
        assertTrue(output[0].contains("modified artifact"), "stdout:" + output[0]);
        assertEquals("", output[1], "stderr:" + output[1]);
    }

    @Test
    public void shouldWarnWhenTheArtifactIsThereButTheVersionIsNot() throws IOException {
        writeParent(properties("lib.version", "4.4"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", null);
        String before = Files.readString(root.resolve("child/pom.xml"));

        String[] output = CommandOutput.capture(() -> assertEquals(0, App.run(new String[]{
                "-P", "-c", "org.acme:lib:3.1:4.0", root.toString()})));

        assertEquals(before, Files.readString(root.resolve("child/pom.xml")));
        assertTrue(output[1].contains("org.acme:lib is declared here but was not changed"),
                "stderr:" + output[1]);
    }

    @Test
    public void shouldNotClaimToRewriteAPropertyTagItCannotFind() throws IOException {
        // the value resolves, but the tag in the file is not the exact text the tool
        // would write back, because of the attribute on it
        write(root.resolve("child/pom.xml"),
                childPom("<properties><lib.version xml:space=\"preserve\">3.1</lib.version></properties>",
                        PROPERTY_VERSION, "root", null));
        String before = Files.readString(root.resolve("child/pom.xml"));

        String[] output = CommandOutput.capture(() -> App.run(new String[]{
                "-c", "org.acme:lib:3.1:4.0", root.toString()}));

        assertFalse(output[0].contains("modified artifact"), "stdout:" + output[0]);
        assertEquals(before, Files.readString(root.resolve("child/pom.xml")));
    }

    private static void assertResolved(String[] output, String version) {
        assertTrue(output[0].contains("org.acme:lib:" + version),
                "expected " + version + ", stdout:" + output[0] + " stderr:" + output[1]);
        assertEquals("", output[1], "stderr:" + output[1]);
    }

    private static void assertUnresolved(String[] output, String warning) {
        assertTrue(output[0].contains("org.acme:lib:" + PROPERTY_VERSION),
                "expected the placeholder, stdout:" + output[0] + " stderr:" + output[1]);
        assertTrue(output[1].contains(warning), "stderr:" + output[1]);
    }

    private String[] run(String... options) throws IOException {
        String[] args = new String[options.length + 1];
        System.arraycopy(options, 0, args, 0, options.length);
        args[options.length] = root.toString();
        return CommandOutput.capture(() -> assertEquals(0, App.run(args), "the run should succeed"));
    }

    private void writeChild(String properties, String dependencyVersion, String parent, String relativePath)
            throws IOException {
        write(root.resolve("child/pom.xml"), childPom(properties, dependencyVersion, parent, relativePath));
    }

    /**
     * Writes a parent pom in {@code folder}, which is the root itself when the
     * folder is empty, and gives it a parent of its own when one is named.
     */
    private void writeParent(String properties, String artifactId, String folder, String parent,
            String relativePath) throws IOException {
        Path pom = folder.isEmpty() ? root.resolve("pom.xml") : root.resolve(folder + "/pom.xml");
        write(pom, parentPom(properties, artifactId, parent, relativePath));
    }

    private void writeParent(String properties, String artifactId, String folder) throws IOException {
        writeParent(properties, artifactId, folder, null, null);
    }

    private static String properties(String name, String value) {
        return "<properties><" + name + ">" + value + "</" + name + "></properties>";
    }

    private static void write(Path pom, String text) throws IOException {
        Files.createDirectories(pom.getParent());
        Files.writeString(pom, text);
    }

    private static String parentPom(String properties, String artifactId, String parent, String relativePath) {
        return "<project>"
                + "<modelVersion>4.0.0</modelVersion>"
                + parentTag(parent, relativePath)
                + "<groupId>com.acme</groupId>"
                + "<artifactId>" + artifactId + "</artifactId>"
                + "<version>5.0</version>"
                + properties
                + "</project>";
    }

    private static String childPom(String properties, String dependencyVersion, String parent,
            String relativePath) {
        return "<project>"
                + "<modelVersion>4.0.0</modelVersion>"
                + parentTag(parent, relativePath)
                + "<artifactId>child</artifactId>"
                + properties
                + "<dependencies>"
                + "<dependency>"
                + "<groupId>org.acme</groupId>"
                + "<artifactId>lib</artifactId>"
                + "<version>" + dependencyVersion + "</version>"
                + "</dependency>"
                + "</dependencies>"
                + "</project>";
    }

    /**
     * A parent named {@code parent}, reaching it through {@code relativePath} when
     * one is given, through the default {@code ../pom.xml} when it is null, and
     * declaring nothing on disk when there is no parent at all.
     */
    private static String parentTag(String parent, String relativePath) {
        if (parent == null) {
            return "";
        }
        String declared = relativePath == null ? "" : "<relativePath>" + relativePath + "</relativePath>";
        return "<parent>"
                + "<groupId>com.acme</groupId>"
                + "<artifactId>" + parent + "</artifactId>"
                + "<version>5.0</version>"
                + declared
                + "</parent>";
    }
}
