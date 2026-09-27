package com.fillumina.maven.reverse.dependency;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PomTreeExtractor implements FileVisitor<Path> {

    private static final Path POM_FILENAME = Paths.get("pom.xml");

    /**
     * Folders that are never a project and are only walked into when the caller
     * asked for every folder, so that a build output directory does not turn a
     * report into a crawl. They are only ever skipped in that mode, because in
     * the other they are left out by the rule below anyway.
     */
    private static final Set<String> NEVER_A_PROJECT = Set.of("target", ".git");

    public static List<Path> readAllPomsInTree(Path path, boolean allFolders) throws IOException {
        PomTreeExtractor visitor = new PomTreeExtractor(allFolders);
        Files.walkFileTree(path, visitor);
        return visitor.paths;
    }

    private final List<Path> paths = new ArrayList<>();
    private final boolean allFolders;
    private boolean firstDir = true;

    private PomTreeExtractor(boolean allFolders) {
        this.allFolders = allFolders;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
        final Path pom = dir.resolve(POM_FILENAME);
        // only the directory that was given is entered when it holds no pom
        // itself, so that a plain folder of projects is scanned. Any other folder
        // without a pom is a folder that groups projects rather than one, and what
        // is in it is not this tool's business. Deciding that from the first
        // directory visited made the answer depend on the order the filesystem
        // happened to list them in.
        boolean isGivenFolder = firstDir;
        firstDir = false;
        if (Files.exists(pom)) {
            paths.add(pom);
            return FileVisitResult.CONTINUE;
        }
        if (isGivenFolder || allFolders) {
            return NEVER_A_PROJECT.contains(dir.getFileName().toString())
                    ? FileVisitResult.SKIP_SUBTREE
                    : FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        throw new IOException("cannot read " + file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

}
