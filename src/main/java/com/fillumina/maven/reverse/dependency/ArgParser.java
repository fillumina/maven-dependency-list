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
        return "[-r] [-m module_regexp] [-d dependecy_regexp] paths...\n" +
                "where:\n" +
                "-r reverse module and dependencies\n" +
                "-m set a module regexp filter\n" +
                "-d set a dependency regexp filter\n" +
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

    @Override
    public String toString() {
        return "arguments passed:" +
                (reverse ? "\nreverse=" + reverse : "") +
                (moduleRegexp != null ? "\nmodule regexp=" + moduleRegexp : "") +
                (dependencyRegexp != null ? "\ndependency regexp=" + dependencyRegexp : "") +
                "\npaths=" + paths.toString();
    }


}
