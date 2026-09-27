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
 * Covers what a version written as a property resolves to, that a parent is only
 * trusted when it is the parent the child declared, and what the tool says when
 * a version stays unresolved.
 */
public class PomInheritanceTest {

    private static final String PROPERTY_VERSION = "${lib.version}";
    private static final String NOT_DEFINED =
            "has version ${lib.version}, which is not defined in this pom.xml"
                    + " or in any parent pom found on disk";

    @TempDir
    Path root;

    @Test
    public void shouldResolveAnInheritedVersion() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", null);

        assertResolved(run(), "3.1");
    }

    @Test
    public void shouldKeepThePlaceholderWhenNoParentDefinesIt() throws IOException {
        writeParent(properties("other.version", "3.1"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", null);

        assertUnresolved(run(), NOT_DEFINED);
    }

    @Test
    public void shouldSayThatNoParentWasFoundOnDisk() throws IOException {
        writeChild("", PROPERTY_VERSION, "root", null);

        assertUnresolved(run(), NOT_DEFINED);
    }

    @Test
    public void shouldNotLookOnDiskWhenTheRelativePathIsEmpty() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", "");

        assertUnresolved(run(), NOT_DEFINED);
    }

    @Test
    public void shouldNotTrustAParentWithAnotherArtifactId() throws IOException {
        // a file that happens to sit at ../pom.xml but is a different project
        writeForeignParent("other:com.other:9.9");
        writeChild("", PROPERTY_VERSION, "root", null);

        assertUnresolved(run(), NOT_DEFINED);
    }

    @Test
    public void shouldNotTrustAParentWithAnotherVersion() throws IOException {
        writeForeignParent("com.acme:root:9.9");
        writeChild("", PROPERTY_VERSION, "root", null);

        assertUnresolved(run(), NOT_DEFINED);
    }

    @Test
    public void shouldNotTrustAParentWithAnotherGroupId() throws IOException {
        writeForeignParent("com.other:root:5.0");
        writeChild("", PROPERTY_VERSION, "root", null);

        assertUnresolved(run(), NOT_DEFINED);
    }

    @Test
    public void shouldFollowACustomRelativePath() throws IOException {
        writeParent(properties("lib.version", "3.1"), "elsewhere", "elsewhere");
        writeChild("", PROPERTY_VERSION, "elsewhere", "../elsewhere/pom.xml");

        assertResolved(run(), "3.1");
    }

    @Test
    public void shouldPreferThePropertyOfTheChildOverTheOneOfTheParent() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild(properties("lib.version", "9.9"), PROPERTY_VERSION, "root", null);

        assertResolved(run(), "9.9");
    }

    @Test
    public void shouldResolveFromAGrandparent() throws IOException {
        writeParent(properties("lib.version", "3.1"), "elsewhere", "elsewhere");
        writeParent("", "middle", "middle", "elsewhere", "../elsewhere/pom.xml");
        writeChild("", PROPERTY_VERSION, "middle", "../middle/pom.xml");

        assertResolved(run(), "3.1");
    }

    @Test
    public void shouldStopOnAParentCycle() throws IOException {
        writeParent(properties("lib.version", "3.1"), "elsewhere", "elsewhere", "child", "../child/pom.xml");
        writeChild("", PROPERTY_VERSION, "elsewhere", "../elsewhere/pom.xml");

        assertResolved(run(), "3.1");
    }

    @Test
    public void shouldNotWarnAboutAPropertyTheModelDefines() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild("", "${project.version}", "root", null);

        String[] output = run();

        assertNoWarnings(output[1]);
        assertTrue(output[0].contains("org.acme:lib:5.0"), "stdout:" + output[0]);
    }

    @Test
    public void shouldShowADependencyItCannotCompareUnderTheOutdatedFilter() throws IOException {
        // nothing to compare against, so it is shown rather than silently dropped
        writeChild("", PROPERTY_VERSION, "root", null);

        String output = CommandOutput.capture(() -> assertEquals(0, App.run(new String[]{
                "-o", "org.acme:lib:9.9", root.toString()})))[0];

        assertTrue(output.contains("com.acme:child"), output);
        assertTrue(output.contains(PROPERTY_VERSION), output);
    }

    @Test
    public void shouldFollowAParentThatInheritsItsOwnCoordinates() throws IOException {
        // the middle pom declares neither a groupId nor a version of its own, so the
        // coordinates the child named are only the ones the middle one inherits
        write(root.resolve("elsewhere/pom.xml"),
                "<project>"
                        + "<modelVersion>4.0.0</modelVersion>"
                        + "<parent><groupId>com.acme</groupId><artifactId>root</artifactId>"
                        + "<version>5.0</version></parent>"
                        + "<artifactId>middle</artifactId>"
                        + properties("lib.version", "3.1")
                        + "</project>");
        writeParent("", "root", "");
        writeChild("", PROPERTY_VERSION, "middle", "../elsewhere/pom.xml");

        assertResolved(run(), "3.1");
    }

    @Test
    public void shouldNotFollowAParentWhoseInheritedCoordinatesDoNotMatch() throws IOException {
        // the middle pom says it inherits from a root at another version
        write(root.resolve("elsewhere/pom.xml"),
                "<project>"
                        + "<modelVersion>4.0.0</modelVersion>"
                        + "<parent><groupId>com.acme</groupId><artifactId>root</artifactId>"
                        + "<version>6.6</version></parent>"
                        + "<artifactId>middle</artifactId>"
                        + properties("lib.version", "3.1")
                        + "</project>");
        writeParent("", "root", "");
        writeChild("", PROPERTY_VERSION, "middle", "../elsewhere/pom.xml");

        assertUnresolved(run(), NOT_DEFINED);
    }

    @Test
    public void shouldPinAnInheritedVersionInTheChild() throws IOException {
        writeParent(properties("lib.version", "3.1"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", null);

        String[] output = CommandOutput.capture(() -> assertEquals(0, App.run(new String[]{
                "-c", "org.acme:lib:3.1:4.0", root.toString()})));

        String child = Files.readString(root.resolve("child/pom.xml"));
        assertTrue(child.contains("<version>4.0</version>"), child);
        assertFalse(child.contains(PROPERTY_VERSION), child);
        assertTrue(output[0].contains("modified artifact"), "stdout:" + output[0]);
        assertNoWarnings(output[1]);
    }

    @Test
    public void shouldWarnWhenTheArtifactIsThereButTheVersionIsNot() throws IOException {
        writeParent(properties("lib.version", "4.4"), "root", "");
        writeChild("", PROPERTY_VERSION, "root", null);
        String before = Files.readString(root.resolve("child/pom.xml"));

        String[] output = CommandOutput.capture(() -> assertEquals(0, App.run(new String[]{
                "-c", "org.acme:lib:3.1:4.0", root.toString()})));

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
        assertNoWarnings(output[1]);
    }

    /**
     * Every run says how much it read on stderr, so a clean run is one with no
     * warning in it rather than one with nothing on it.
     */
    private static void assertNoWarnings(String stderr) {
        assertFalse(stderr.contains("WARNING"), stderr);
    }

    private static void assertUnresolved(String[] output, String warning) {
        assertTrue(output[0].contains("org.acme:lib:" + PROPERTY_VERSION),
                "expected the placeholder, stdout:" + output[0] + " stderr:" + output[1]);
        assertTrue(output[1].contains(warning), "stderr:" + output[1]);
    }

    private String[] run() throws IOException {
        return CommandOutput.capture(() -> assertEquals(0, App.run(new String[]{root.toString()}),
                "the run should succeed"));
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

    /**
     * Writes a pom at the path the child expects its parent at, but for another
     * project entirely, so that it must not be trusted.
     */
    private void writeForeignParent(String coordinates) throws IOException {
        String[] fields = coordinates.split(":");
        write(root.resolve("pom.xml"),
                "<project>"
                        + "<modelVersion>4.0.0</modelVersion>"
                        + "<groupId>" + fields[0] + "</groupId>"
                        + "<artifactId>" + fields[1] + "</artifactId>"
                        + "<version>" + fields[2] + "</version>"
                        + properties("lib.version", "3.1")
                        + "</project>");
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
