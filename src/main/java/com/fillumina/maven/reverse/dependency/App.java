package com.fillumina.maven.reverse.dependency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class App {

    public static void main(String[] args) {
        int status = run(args);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args) {
        ArgParser arguments = null;
        try {
            arguments = new ArgParser(args);
            execution(arguments);
            return 0;
        } catch (Throwable t) {
            if (arguments != null && arguments.isFullStacktrace()) {
                t.printStackTrace();
            } else {
                System.err.println("ERROR: " + t.getMessage());
            }
            return 1;
        }
    }

    /**
     * Whether the text mentions the artifact at all, so a sweep can stay quiet about
     * the poms that do not use it and speak up about the ones it failed to change.
     */
    private static boolean declares(String pomContent, PackageId artifact) {
        return pomContent.contains("<artifactId>" + artifact.getArtifactId() + "</artifactId>");
    }

    /**
     * Drops everything that is not the wanted artifact, and every project left
     * with nothing, so that a report is only about what is behind. In the reverse
     * view the dependency is the association and the projects are what hangs off
     * it, so there the whole entry goes or stays.
     */
    private static void keepOnlyOutdated(Map<String, Association> associations, PackageId wanted,
            boolean reverse) {
        if (reverse) {
            associations.values().removeIf(association -> !isBehind(association.getProject(), wanted));
            return;
        }
        associations.values().forEach(association ->
                association.getSet().removeIf(dependency -> !isBehind(dependency, wanted)));
        associations.values().removeIf(association -> association.getSet().isEmpty());
    }

    private static boolean isBehind(PackageId dependency, PackageId wanted) {
        if (!dependency.getName().equals(wanted.getName())) {
            return false;
        }
        if (dependency.getVersion() == null || wanted.getVersion() == null) {
            // nothing to compare, so the dependency is shown rather than hidden
            return true;
        }
        return VersionComparator.compare(dependency.getVersion(), wanted.getVersion()) < 0;
    }

    /**
     * Says how much of what was passed was actually read, so that a short listing
     * can be told apart from a tree that was not searched. A project that declares
     * nothing gets no line of its own unless `-n` is used, so the count of poms and
     * the count of lines are not the same, and printing one without the other would
     * be how a tree that was not searched looks like a tree with nothing to report.
     *
     * @param entries the lines printed, or -1 when the run changed poms instead
     */
    private static void reportCoverage(int poms, int entries) {
        if (poms == 0) {
            return;
        }
        if (entries < 0) {
            System.err.println("read " + poms + " pom.xml");
            return;
        }
        System.err.println("read " + poms + " pom.xml, listed " + entries
                + " entries. A project that declares nothing gets no line of its own"
                + " unless -n is used.");
    }

    static void execution(ArgParser arguments) throws IOException {
        if (arguments.isError() || arguments.isHelp()) {
            System.out.println(ArgParser.getUsage());

        } else {
            System.out.println(arguments);

            List<Path> pomPaths = new ArrayList<>();

            if (arguments.isReverse()) {
                System.out.println("\nprojects using dependencies\n");
            } else {
                System.out.println("\ndependencies used by project\n");
            }

            for (String folderName : arguments.getFolderNames()) {
                System.out.println("searching in: " + folderName);
                Path path = Paths.get(folderName);
                pomPaths.addAll(PomTreeExtractor.readAllPomsInTree(path, arguments.isAllFolders()));
            }

            final Pattern moduleRegexp = arguments.getModuleRegexp();

            AssociationBuilder associationBuilder = new AssociationBuilder(
                    moduleRegexp,
                    arguments.getDependencyRegexp(),
                    arguments.isReverse(),
                    arguments.isOmitNullVersion());

            System.out.println("");
            if (pomPaths.isEmpty()) {
                System.err.println("WARNING: no pom.xml was read at all, "
                        + "so nothing was searched and nothing below was found.");
                if (!arguments.isAllFolders()) {
                    System.err.println("         A folder that only groups projects is not entered."
                            + " If the projects are under plain folders,");
                    System.err.println("         " + ArgParser.ALL_FOLDERS_HINT
                            + " looks inside those too.");
                }
            }

            final boolean noDependencies = arguments.isNoDependencies();
            final Path repository = arguments.getRepository();
            final PackageId artifactToChange = arguments.getArtifactToChange();
            final String newVersion = arguments.getNewVersion();
            final boolean changeArtifactMode = artifactToChange != null && newVersion != null;
            final boolean makeBackupCopy = arguments.isMakeBackupCopy();

            for (Path pomPath : pomPaths) {
                String pomContent = Files.readString(pomPath);
                if (changeArtifactMode) {
                    Pom pom = new Pom(pomContent, pomPath, repository,
                            associationBuilder, true);
                    if (moduleRegexp != null) {
                        PackageId pkg = pom.getPomPackage();
                        String pkgName = pkg.toString();
                        if (moduleRegexp != null && !moduleRegexp.matcher(pkgName).matches()) {
                            System.out.println("skipping " + pkgName + " ...");
                            continue;
                        }
                    }
                    CharSequence modifiedPom = PomModifier.INSTANCE.modify(
                            pomContent, pom.getPropertyMap(), pom.getOwnPropertyMap(),
                            artifactToChange, newVersion);
                    if (modifiedPom == null && declares(pomContent, artifactToChange)) {
                        System.err.println("WARNING: " + pomPath + ": " + artifactToChange.getName()
                                + " is declared here but was not changed, because its version is not "
                                + artifactToChange.getVersion()
                                + " or it is a property this file cannot rewrite");
                    }
                    if (modifiedPom != null) {
                        if (makeBackupCopy) {
                            final File pomFile = pomPath.toAbsolutePath().normalize().toFile();
                            String bkFilename = pomFile.getCanonicalPath() + ".bak";
                            File bkPomFile = new File(bkFilename);
                            System.out.println("backup " + pomFile.toString() + " -> " + bkPomFile.toString());
                            Files.move(pomFile.toPath(), bkPomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                        Files.writeString(pomPath, modifiedPom);
                        System.out.println("modified artifact in " + pomPath.toString());
                    }
                } else {
                    new Pom(pomContent, pomPath, repository, associationBuilder, noDependencies);
                }
            }

            if (!changeArtifactMode) {
                final PackageId outdated = arguments.getOutdatedArtifact();
                if (outdated != null) {
                    keepOnlyOutdated(associationBuilder.getMap(), outdated, arguments.isReverse());
                }
                if (noDependencies) {
                    associationBuilder.getMap().values().stream().forEach(System.out::print);
                } else {
                    associationBuilder.getMap().values().stream().forEach(System.out::println);
                }
                reportCoverage(pomPaths.size(), associationBuilder.getMap().size());
            } else {
                reportCoverage(pomPaths.size(), -1);
            }
        }
    }

}
