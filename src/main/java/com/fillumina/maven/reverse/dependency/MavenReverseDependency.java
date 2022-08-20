package com.fillumina.maven.reverse.dependency;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MavenReverseDependency {

    public static void main(String[] args) throws IOException {
        ArgParser arguments = new ArgParser(args);
        if (arguments.isError() || arguments.isHelp()) {
            System.out.println(ArgParser.getUsage());

        } else {
            System.out.println(arguments);

            List<Path> poms = new ArrayList<>();

            if (arguments.isReverse()) {
                System.out.println("\nmodules using dependencies\n");
            } else {
                System.out.println("\ndependencies use by module\n");
            }

            for (String folderName : arguments.getFolderNames()) {
                System.out.println("searching in: " + folderName);
                Path path = Paths.get(folderName);
                poms.addAll(PomTreeExtractor.readAllPomsInTree(path));
            }

            AssociationBuilder associationBuilder = new AssociationBuilder(
                    arguments.getModuleRegexp(),
                    arguments.getDependencyRegexp(),
                    arguments.isReverse());

            System.out.println("");
            poms.forEach(p -> PomLoader.INSTANCE.loader(p.toFile(), associationBuilder,
                    arguments.isNoDependencies()));

            if (arguments.isNoDependencies()) {
                associationBuilder.getMap().values().stream().forEach(System.out::print);
            } else {
                associationBuilder.getMap().values().stream().forEach(System.out::println);
            }

        }
    }


}
