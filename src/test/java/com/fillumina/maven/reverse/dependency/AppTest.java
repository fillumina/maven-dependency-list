package com.fillumina.maven.reverse.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Drives {@link App#execution} over a directory of poms written on the fly, covering the paths
 * that rewrite files and the flag combinations that feed {@link AssociationBuilder} a null
 * dependency.
 */
public class AppTest {

    private static final String LONG_VERSION = "4.13.2-very-long-old-version";
    private static final String PROJECT = "com.acme:demo:1.0-SNAPSHOT";

    @TempDir
    Path root;

    @Test
    public void shouldTruncateThePomWhenTheNewVersionIsShorter() throws IOException {
        Path pom = writePom("proj", LONG_VERSION);

        App.execution(new ArgParser(change(LONG_VERSION, "4.9")));

        assertEquals(pomText("4.9"), Files.readString(pom));
    }

    @Test
    public void shouldWriteTheWholePomWhenTheNewVersionIsLonger() throws IOException {
        Path pom = writePom("proj", "4.9");

        App.execution(new ArgParser(change("4.9", LONG_VERSION)));

        assertEquals(pomText(LONG_VERSION), Files.readString(pom));
    }

    @Test
    public void shouldKeepNonAsciiCharacters() throws IOException {
        Path pom = writePom("proj", LONG_VERSION);
        String text = Files.readString(pom).replace("<name>demo</name>", "<name>caffè</name>");
        Files.writeString(pom, text);

        App.execution(new ArgParser(change(LONG_VERSION, "4.9")));

        assertEquals(pomText("4.9").replace("<name>demo</name>", "<name>caffè</name>"),
                Files.readString(pom));
    }

    @Test
    public void shouldMoveTheOriginalPomAsideWhenMakingABackupCopy() throws IOException {
        Path pom = writePom("proj", LONG_VERSION);
        String original = Files.readString(pom);
        String[] args = change(LONG_VERSION, "4.9");
        String[] withBackup = new String[args.length + 1];
        System.arraycopy(args, 0, withBackup, 0, args.length);
        withBackup[withBackup.length - 1] = "-b";

        App.execution(new ArgParser(withBackup));

        assertEquals(original, Files.readString(pom.resolveSibling("pom.xml.bak")));
        assertEquals(pomText("4.9"), Files.readString(pom));
    }

    @Test
    public void shouldListTheProjectsWhenOmittingNullVersions() throws IOException {
        writePom("proj", LONG_VERSION);

        String output = CommandOutput.capture(() -> App.execution(new ArgParser(new String[]{"-n", "-v", root.toString()})))[0];

        assertTrue(output.contains(PROJECT), output);
    }

    @Test
    public void shouldListNothingForProjectsWithoutDependenciesInReverseMode() throws IOException {
        writePom("proj", LONG_VERSION);

        String output = CommandOutput.capture(() -> App.execution(new ArgParser(new String[]{"-n", "-r", root.toString()})))[0];

        assertFalse(output.contains(PROJECT), output);
    }

    @Test
    public void shouldKeepOnlyTheProjectsBehindTheGivenVersion() throws IOException {
        writeService("old-service", "4.13.2");
        writeService("current-service", "5.9.0");

        String output = CommandOutput.capture(() -> App.run(new String[]{"-o", "junit:junit:5.6.0", root.toString()}))[0];

        assertTrue(output.contains("com.acme:old-service"), output);
        assertFalse(output.contains("com.acme:current-service"), output);
    }

    @Test
    public void shouldListNothingWhenEveryProjectIsUpToDate() throws IOException {
        writeService("a", "5.9.0");
        writeService("b", "5.9.1");

        String output = CommandOutput.capture(() -> App.run(new String[]{"-o", "junit:junit:5.6.0", root.toString()}))[0];

        assertFalse(output.contains("com.acme:a"), output);
        assertFalse(output.contains("com.acme:b"), output);
    }

    @Test
    public void shouldListNothingForAnotherArtifact() throws IOException {
        writeService("a", "4.13.2");

        String output = CommandOutput.capture(() -> App.run(new String[]{"-o", "org.other:lib:9.9", root.toString()}))[0];

        assertFalse(output.contains("com.acme:a"), output);
    }

    @Test
    public void shouldCountAnEqualVersionAsUpToDate() throws IOException {
        writeService("a", "5.6.0");

        String output = CommandOutput.capture(() -> App.run(new String[]{"-o", "junit:junit:5.6.0", root.toString()}))[0];

        assertFalse(output.contains("com.acme:a"), output);
    }

    @Test
    public void shouldKeepOnlyTheOlderVersionInTheReverseView() throws IOException {
        writeService("old-service", "4.13.2");
        writeService("current-service", "5.9.0");

        String output = CommandOutput.capture(() -> App.run(
                new String[]{"-r", "-o", "junit:junit:5.6.0", root.toString()}))[0];

        assertTrue(output.contains("junit:junit:4.13.2"), output);
        assertFalse(output.contains("junit:junit:5.9.0"), output);
        assertTrue(output.contains("com.acme:old-service"), output);
        assertFalse(output.contains("com.acme:current-service"), output);
    }

    @Test
    public void shouldSayHowMuchItRead() throws IOException {
        writeService("a", "1.0");
        writeService("b", "1.0");

        String[] captured = CommandOutput.capture(() -> assertEquals(0, App.run(new String[]{root.toString()})));

        assertTrue(captured[1].contains("read 2 pom.xml, listed 2 entries"), captured[1]);
    }

    @Test
    public void shouldExplainWhyThereAreFewerEntriesThanPoms() throws IOException {
        writePom("proj", LONG_VERSION);
        Path empty = root.resolve("no-deps");
        Files.createDirectories(empty);
        Files.writeString(empty.resolve("pom.xml"),
                "<project><modelVersion>4.0.0</modelVersion><groupId>com.acme</groupId>"
                        + "<artifactId>boring</artifactId><version>1</version></project>");

        String[] captured = CommandOutput.capture(() -> assertEquals(0, App.run(new String[]{root.toString()})));

        assertTrue(captured[1].contains("read 2 pom.xml, listed 1 entries"), captured[1]);
        assertTrue(captured[1].contains("unless -n is used"), captured[1]);
    }

    @Test
    public void shouldSayWhenItReadNothingAtAll() throws IOException {
        // a folder that exists and holds no pom is not an error, so without this
        // line it is indistinguishable from a tree with nothing to report
        Path empty = root.resolve("no-projects-here");
        Files.createDirectories(empty);

        String[] captured = CommandOutput.capture(() ->
                assertEquals(0, App.run(new String[]{empty.toString()})));

        assertTrue(captured[1].contains("no pom.xml was read at all"), captured[1]);
    }

    @Test
    public void shouldPointAtAllFoldersWhenItReadNothing() throws IOException {
        // reading nothing is usually because the tree is grouped under plain folders,
        // and the fix is a flag the reader cannot guess from an empty listing
        Path noProjects = root.resolve("grouped");
        Files.createDirectories(noProjects);

        String[] captured = CommandOutput.capture(() ->
                assertEquals(0, App.run(new String[]{noProjects.toString()})));

        assertTrue(captured[1].contains("no pom.xml was read at all"), captured[1]);
        assertTrue(captured[1].contains("-a looks inside those too"), captured[1]);
    }

    @Test
    public void shouldNotSuggestAllFoldersWhenTheyWereAlreadyGiven() throws IOException {
        Path noProjects = root.resolve("grouped");
        Files.createDirectories(noProjects);

        String[] captured = CommandOutput.capture(() ->
                assertEquals(0, App.run(new String[]{"-a", noProjects.toString()})));

        assertTrue(captured[1].contains("no pom.xml was read at all"), captured[1]);
        assertFalse(captured[1].contains("-a looks inside"), captured[1]);
    }

    @Test
    public void shouldCountThePomsItChangedInChangeMode() throws IOException {
        Path pom = writePom("proj", LONG_VERSION);

        String[] captured = CommandOutput.capture(() -> App.run(new String[]{
                "-c", "junit:junit:" + LONG_VERSION + ":4.9", root.toString()}));

        assertTrue(captured[1].contains("read 1 pom.xml"), captured[1]);
        assertFalse(captured[1].contains("listed"), captured[1]);
        assertTrue(Files.readString(pom).contains("<version>4.9</version>"));
    }

    @Test
    public void shouldExitZeroOnSuccess() {
        writePomUnchecked("proj", LONG_VERSION);

        assertEquals(0, App.run(new String[]{root.toString()}));
    }

    @Test
    public void shouldFailOnAPathThatDoesNotExist() throws IOException {
        String[] captured = CommandOutput.capture(() -> assertEquals(1, App.run(new String[]{root.resolve("absent").toString()})));

        assertTrue(captured[1].startsWith("ERROR: cannot read "), captured[1]);
        assertFalse(captured[0].contains("TERMINATE"), captured[0]);
    }

    @Test
    public void shouldReportAMalformedArgumentInOneLine() throws IOException {
        String[] captured = CommandOutput.capture(() -> assertEquals(1, App.run(new String[]{"-c", "a:b:c", root.toString()})));

        assertEquals("ERROR: expected 4 fields separated by ':', was= 'a:b:c'" + System.lineSeparator(),
                captured[1]);
        assertFalse(captured[1].contains("\tat "), captured[1]);
    }

    @Test
    public void shouldReportAnInvalidRegexpInOneLine() throws IOException {
        String[] captured = CommandOutput.capture(() -> assertEquals(1, App.run(new String[]{"-p", "[", root.toString()})));

        assertTrue(captured[1].startsWith("ERROR: "), captured[1]);
        assertFalse(captured[1].contains("\tat "), captured[1]);
    }

    @Test
    public void shouldReportAMalformedArgumentInOneLineEvenWithJ() throws IOException {
        String[] captured = CommandOutput.capture(() -> assertEquals(1, App.run(new String[]{"-j", "-c", "a:b:c", root.toString()})));

        assertEquals("ERROR: expected 4 fields separated by ':', was= 'a:b:c'" + System.lineSeparator(),
                captured[1]);
    }

    private void writePomUnchecked(String folder, String version) {
        try {
            writePom(folder, version);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String[] change(String oldVersion, String newVersion) {
        return new String[]{"-c", "junit:junit:" + oldVersion + ":" + newVersion, root.toString()};
    }

    private void writeService(String name, String junitVersion) throws IOException {
        Path folder = root.resolve(name);
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("pom.xml"),
                "<project>"
                        + "<modelVersion>4.0.0</modelVersion>"
                        + "<groupId>com.acme</groupId>"
                        + "<artifactId>" + name + "</artifactId>"
                        + "<version>1.0</version>"
                        + "<dependencies>"
                        + "<dependency>"
                        + "<groupId>junit</groupId>"
                        + "<artifactId>junit</artifactId>"
                        + "<version>" + junitVersion + "</version>"
                        + "</dependency>"
                        + "</dependencies>"
                        + "</project>");
    }

    private Path writePom(String folder, String version) throws IOException {
        Path folderPath = root.resolve(folder);
        Files.createDirectories(folderPath);
        Path pom = folderPath.resolve("pom.xml");
        Files.writeString(pom, pomText(version));
        return pom;
    }

    private static String pomText(String version) {
        return "<project>"
                + "<modelVersion>4.0.0</modelVersion>"
                + "<groupId>com.acme</groupId>"
                + "<artifactId>demo</artifactId>"
                + "<version>1.0-SNAPSHOT</version>"
                + "<name>demo</name>"
                + "<dependencies>"
                + "<dependency>"
                + "<groupId>junit</groupId>"
                + "<artifactId>junit</artifactId>"
                + "<version>" + version + "</version>"
                + "</dependency>"
                + "</dependencies>"
                + "</project>";
    }

}
