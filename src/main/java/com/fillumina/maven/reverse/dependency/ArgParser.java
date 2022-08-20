package com.fillumina.maven.reverse.dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ArgParser {

    private boolean help;
    private boolean error;
    private boolean reverse;
    private boolean noDependencies;
    private Pattern moduleRegexp;
    private Pattern dependencyRegexp;
    private List<String> paths = new ArrayList<>();

    public ArgParser(String[] args) {
        boolean module = false, dependence = false;
        if (args == null || args.length == 0) {
            error = true;
        } else {
            for (String s : args) {
                if ("-h".equals(s) || "--help".equals(s)) {
                    help = true;
                } else if (module) {
                    moduleRegexp = Pattern.compile("^.*" + s + ".*$");
                    module = false;
                } else if (dependence) {
                    dependencyRegexp = Pattern.compile("^.*" + s + ".*$");
                    dependence = false;
                } else {
                    switch (s) {
                        case "-r" -> {
                            reverse = true;
                        }
                        case "-n" -> {
                            noDependencies = true;
                        }
                        case "-m" -> {
                            module = true;
                        }
                        case "-d" -> {
                            dependence = true;
                        }
                        default -> {
                            paths.add(s);
                        }
                    }
                }
            }
        }
    }

    public static String getUsage() {
        return "by Francesco Illuminati fillumina@gmail.com, ver 1.1 19-8-22\n" +
                "[-r] [-n] [-m module_regexp] [-d dependecy_regexp] paths...\n" +
                "where:\n" +
                "-r reverse module and dependencies\n" +
                "-n print only project names without dependencies\n" +
                "-m regexp set a module filter\n" +
                "-d regexp set a dependency filter\n" +
                "paths... path list to search for pom.xml\n";
    }

    public boolean isReverse() {
        return reverse;
    }

    public Pattern getModuleRegexp() {
        return moduleRegexp;
    }

    public Pattern getDependencyRegexp() {
        return dependencyRegexp;
    }

    public List<String> getFolderNames() {
        return paths;
    }

    public boolean isHelp() {
        return help;
    }

    public boolean isError() {
        return error;
    }

    public boolean isNoDependencies() {
        return noDependencies;
    }

    @Override
    public String toString() {
        return "arguments passed:" +
                (reverse ? "\nreverse=" + reverse : "") +
                (noDependencies ? "\no dependencies=" + noDependencies : "") +
                (moduleRegexp != null ? "\nmodule regexp=" + moduleRegexp : "") +
                (dependencyRegexp != null ? "\ndependency regexp=" + dependencyRegexp : "") +
                "\npaths=" + paths.toString();
    }


}
