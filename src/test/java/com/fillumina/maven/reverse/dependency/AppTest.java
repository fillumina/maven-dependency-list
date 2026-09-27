package com.fillumina.maven.reverse.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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

        String output = captureStdOut(() -> App.execution(new ArgParser(new String[]{"-n", "-v", root.toString()})));

        assertTrue(output.contains(PROJECT), output);
    }

    @Test
    public void shouldListNothingForProjectsWithoutDependenciesInReverseMode() throws IOException {
        writePom("proj", LONG_VERSION);

        String output = captureStdOut(() -> App.execution(new ArgParser(new String[]{"-n", "-r", root.toString()})));

        assertFalse(output.contains(PROJECT), output);
    }

    private String[] change(String oldVersion, String newVersion) {
        return new String[]{"-c", "junit:junit:" + oldVersion + ":" + newVersion, root.toString()};
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

    private static String captureStdOut(IoRunnable runnable) throws IOException {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            runnable.run();
        } finally {
            System.setOut(original);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface IoRunnable {
        void run() throws IOException;
    }
}
