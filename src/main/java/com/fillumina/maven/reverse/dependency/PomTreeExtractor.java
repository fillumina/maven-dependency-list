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

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PomTreeExtractor implements FileVisitor<Path> {

    private static final Path POM_FILENAME = Paths.get("pom.xml");

    public static List<Path> readAllPomsInTree(Path path) throws IOException {
        PomTreeExtractor visitor = new PomTreeExtractor();
        Files.walkFileTree(path, visitor);
        return visitor.paths;
    }

    private final List<Path> paths = new ArrayList<>();
    private boolean firstDir = true;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
        final Path pom = dir.resolve(POM_FILENAME);
        if (Files.exists(pom)) {
            paths.add(pom);
            return FileVisitResult.CONTINUE;
        } else if (firstDir) {
            firstDir = false;
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.out.println("TERMINATE ON " + file);
        return FileVisitResult.TERMINATE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

}
