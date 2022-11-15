package com.fillumina.maven.reverse.dependency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class App {

    public static void main(String[] args) throws IOException {
        ArgParser arguments = new ArgParser(args);
        try {
            execution(arguments);
        } catch (Throwable t) {
            if (arguments.isFullStacktrace()) {
                t.printStackTrace();
            } else {
                System.err.println("ERROR: " + t.getMessage());
            }
            System.exit(1);
        }
    }

    private static void execution(ArgParser arguments) throws IOException {
        if (arguments.isError() || arguments.isHelp()) {
            System.out.println(ArgParser.getUsage());

        } else {
            System.out.println(arguments);

            List<Path> pomPaths = new ArrayList<>();

            if (arguments.isReverse()) {
                System.out.println("\nmodules using dependencies\n");
            } else {
                System.out.println("\ndependencies use by module\n");
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
                    Pom pom = new Pom(pomContent, associationBuilder, true);
                    if (moduleRegexp != null) {
                        PackageId pkg = pom.getPomPackage();
                        String pkgName = pkg.toString();
                        if (moduleRegexp != null && !moduleRegexp.matcher(pkgName).matches()) {
                            System.out.println("skipping " + pkgName + " ...");
                            continue;
                        }
                    }
                    CharSequence modifiedPom = PomModifier.INSTANCE.modify(
                            pomContent, pom.getPropertyMap(), artifactToChange, newVersion);
                    if (modifiedPom != null) {
                        if (makeBackupCopy) {
                            final File pomFile = pomPath.toAbsolutePath().normalize().toFile();
                            String bkFilename = pomFile.getCanonicalPath() + ".bak";
                            File bkPomFile = new File(bkFilename);
                            System.out.println("backup " + pomFile.toString() + " -> " + bkPomFile.toString());
                            Files.move(pomFile.toPath(), bkPomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                        Files.writeString(pomPath, modifiedPom, StandardOpenOption.CREATE);
                        System.out.println("modified artifact in " + pomPath.toString());
                    }
                } else {
                    new Pom(pomContent, associationBuilder, noDependencies);
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
