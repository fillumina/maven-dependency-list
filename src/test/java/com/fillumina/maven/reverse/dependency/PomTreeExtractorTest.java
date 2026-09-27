package com.fillumina.maven.reverse.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Covers which {@code pom.xml} files a tree walk finds, and just as much which ones
 * it does not.
 */
public class PomTreeExtractorTest {

    @TempDir
    Path root;

    @Test
    public void shouldFindTheProjectsInAFolderThatIsNotAProject() throws IOException {
        writePom(root.resolve("a/pom.xml"));
        writePom(root.resolve("b/pom.xml"));

        assertEquals(List.of(root.resolve("a/pom.xml"), root.resolve("b/pom.xml")), found());
    }

    @Test
    public void shouldFindAProjectThatIsTheGivenFolderItself() throws IOException {
        writePom(root.resolve("pom.xml"));
        writePom(root.resolve("a/pom.xml"));

        List<Path> found = found();

        assertTrue(found.contains(root.resolve("pom.xml")), found.toString());
        assertTrue(found.contains(root.resolve("a/pom.xml")), found.toString());
    }

    @Test
    public void shouldNotDescendIntoAFolderThatGroupsProjects() throws IOException {
        // tools/ is not a project, so the project inside it is not this tool's
        // business. The given folder is a project itself here, which is the case
        // that used to let the next folder without a pom be entered as well and
        // made the answer depend on the order the filesystem listed them in.
        writePom(root.resolve("pom.xml"));
        writePom(root.resolve("a/pom.xml"));
        writePom(root.resolve("tools/codegen/pom.xml"));
        writePom(root.resolve("zzz/pom.xml"));

        assertEquals(List.of(root.resolve("a/pom.xml"), root.resolve("pom.xml"),
                root.resolve("zzz/pom.xml")), found());
    }

    @Test
    public void shouldDescendIntoSubfoldersOfAProject() throws IOException {
        // once a directory is a project, the modules under it are projects too
        writePom(root.resolve("pom.xml"));
        writePom(root.resolve("module-a/pom.xml"));
        writePom(root.resolve("module-a/nested/pom.xml"));

        assertEquals(3, found().size());
    }

    @Test
    public void shouldEnterTheGroupingFoldersWhenAskedTo() throws IOException {
        writePom(root.resolve("pom.xml"));
        writePom(root.resolve("a/pom.xml"));
        writePom(root.resolve("tools/codegen/pom.xml"));

        List<Path> found = PomTreeExtractor.readAllPomsInTree(root, true).stream().sorted().toList();

        assertTrue(found.contains(root.resolve("tools/codegen/pom.xml")), found.toString());
    }

    @Test
    public void shouldStillLeaveTargetAndGitAloneWhenEnteringEveryFolder() throws IOException {
        writePom(root.resolve("pom.xml"));
        writePom(root.resolve("target/shaded/pom.xml"));
        writePom(root.resolve(".git/hooks/pom.xml"));
        writePom(root.resolve("tools/codegen/pom.xml"));

        List<Path> found = PomTreeExtractor.readAllPomsInTree(root, true).stream().sorted().toList();

        assertEquals(List.of(root.resolve("pom.xml"), root.resolve("tools/codegen/pom.xml")), found);
    }

    @Test
    public void shouldFailWhenTheFolderItWasGivenCannotBeRead() {
        // on unix the permissions of a temporary folder say nothing useful, so this
        // is the closest a test can get: a folder that is not there at all
        assertThrows(UncheckedIOException.class,
                () -> PomTreeExtractor.readAllPomsInTree(root.resolve("absent"), false));
    }

    @Test
    public void shouldCarryOnPastAFolderItCannotReadOnTheWay() throws IOException {
        // a folder the walk runs into that cannot be read is reported and stepped
        // over, because losing a whole survey to one of them helps nobody
        writePom(root.resolve("a/pom.xml"));
        Path unreadable = root.resolve("locked");
        Files.createDirectories(unreadable);
        writePom(unreadable.resolve("pom.xml"));
        unreadable.toFile().setReadable(false);
        try {
            List<Path> found = PomTreeExtractor.readAllPomsInTree(root, true).stream().sorted().toList();

            assertTrue(found.contains(root.resolve("a/pom.xml")), found.toString());
        } finally {
            unreadable.toFile().setReadable(true);
        }
    }

    @Test
    public void shouldNotReadAPomThatShipsAsAResource() throws IOException {
        writePom(root.resolve("pom.xml"));
        writePom(root.resolve("generator/src/main/resources/pom.xml"));
        writePom(root.resolve("a/pom.xml"));

        assertEquals(List.of(root.resolve("a/pom.xml"), root.resolve("pom.xml")),
                PomTreeExtractor.readAllPomsInTree(root, true).stream().sorted().toList());
    }

    @Test
    public void shouldNotEnterAGroupingFolderWhenNotAskedTo() throws IOException {
        writePom(root.resolve("pom.xml"));
        writePom(root.resolve("tools/codegen/pom.xml"));

        assertEquals(List.of(root.resolve("pom.xml")),
                PomTreeExtractor.readAllPomsInTree(root, false).stream().sorted().toList());
    }

    private List<Path> found() throws IOException {
        return PomTreeExtractor.readAllPomsInTree(root, false).stream().sorted().toList();
    }

    private static void writePom(Path pom) throws IOException {
        Files.createDirectories(pom.getParent());
        Files.writeString(pom, "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>com.acme</groupId><artifactId>x</artifactId><version>1</version></project>");
    }
}
