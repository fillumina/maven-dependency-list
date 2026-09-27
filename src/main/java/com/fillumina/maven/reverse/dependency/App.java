package com.fillumina.maven.reverse.dependency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
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
                pomPaths.addAll(PomTreeExtractor.readAllPomsInTree(path));
            }

            final Pattern moduleRegexp = arguments.getModuleRegexp();

            AssociationBuilder associationBuilder = new AssociationBuilder(
                    moduleRegexp,
                    arguments.getDependencyRegexp(),
                    arguments.isReverse(),
                    arguments.isOmitNullVersion());

            System.out.println("");

            final boolean noDependencies = arguments.isNoDependencies();
            final PackageId artifactToChange = arguments.getArtifactToChange();
            final String newVersion = arguments.getNewVersion();
            final boolean changeArtifactMode = artifactToChange != null && newVersion != null;
            final boolean makeBackupCopy = arguments.isMakeBackupCopy();

            for (Path pomPath : pomPaths) {
                String pomContent = Files.readString(pomPath);
                if (changeArtifactMode) {
                    Pom pom = new Pom(pomContent, pomPath, associationBuilder, true);
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
                    new Pom(pomContent, pomPath, associationBuilder, noDependencies);
                }
            }

            if (!changeArtifactMode) {
                if (noDependencies) {
                    associationBuilder.getMap().values().stream().forEach(System.out::print);
                } else {
                    associationBuilder.getMap().values().stream().forEach(System.out::println);
                }
            }
        }
    }

}
