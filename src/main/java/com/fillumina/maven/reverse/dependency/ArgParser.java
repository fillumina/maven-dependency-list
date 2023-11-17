package com.fillumina.maven.reverse.dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ArgParser {
    private static final String VERSION = "1.2.1";
    private static final String VERSION_DATA = "15/11/22";

    private static final String HELP_LONG = "--help";
    private static final String HELP_SHORT = "-h";
    private static final String FULL_STACKTRACE = "-j";
    private static final String CHANGE_ARTIFACT = "-c";
    private static final String BACKUP_COPY = "-b";
    private static final String DEPENDENCY = "-d";
    private static final String MODULE = "-m";
    private static final String NO_DEPENCENCIES = "-n";
    private static final String REVERSE = "-r";
    private static final String OMIT_NULL_VERSION = "-v";

    private static final String USAGE =
        "by Francesco Illuminati fillumina@gmail.com - https://github.com/fillumina/maven-dependency-list " +
            "- ver " + VERSION + " " + VERSION_DATA +"\n" +
        "List and change package versions in a directory tree of maven pom.xml with filters.\n" +
        "options: [" + HELP_SHORT + "|" + HELP_LONG + "] [" + REVERSE + "] [" + NO_DEPENCENCIES +"] [" + MODULE + " module_regexp] " +
            "[" + DEPENDENCY + " dependecy_regexp] [" + CHANGE_ARTIFACT + " group:artifact:ver:new-ver] " +
            "[" + BACKUP_COPY + "] paths...\n" +
        "where:\n" +
        HELP_SHORT + " or " + HELP_LONG + " print this help\n" +
        REVERSE + " reverse module and dependencies\n" +
        NO_DEPENCENCIES + " print only project names without dependencies\n" +
        MODULE + " regexp set a module filter\n" +
        DEPENDENCY + " regexp set a dependency/plugin filter\n" +
        OMIT_NULL_VERSION + " omit dependencies/plugins with null version\n" +
        CHANGE_ARTIFACT + " group:artifact:ver:new-ver change version of all package occurences\n" +
        "   cannot be mixed with dependency filter (" + DEPENDENCY + "), can use module filtering (" + MODULE + ")\n" +
        BACKUP_COPY + " make a backup copy of the changed pom.xml.equals(s)) pom.xml.bak (only with " + CHANGE_ARTIFACT + ")\n" +
        FULL_STACKTRACE + " print a full java exception stacktrace\n" +
        "paths... path list to search for pom.xml\n";

    private boolean help;
    private boolean error;
    private boolean reverse;
    private boolean noDependencies;
    private Pattern moduleRegexp;
    private Pattern dependencyRegexp;
    private List<String> paths = new ArrayList<>();
    private PackageId artifactToChange;
    private String newVersion;
    private boolean makeBackupCopy;
    private boolean fullStacktrace;
    private boolean omitNullVersion;

    public ArgParser(String[] args) {
        boolean module = false, dependency = false, changeArtifact = false;
        if (args == null || args.length == 0) {
            error = true;
        } else {
            for (String s : args) {
                if (HELP_SHORT.equals(s) || HELP_LONG.equals(s)) {
                    help = true;
                } else if (module) {
                    moduleRegexp = Pattern.compile("^.*" + s + ".*$");
                    module = false;
                } else if (dependency) {
                    dependencyRegexp = Pattern.compile("^.*" + s + ".*$");
                    dependency = false;
                } else if (changeArtifact) {
                    String[] fields = s.split(":");
                    if (fields.length == 4) {
                        artifactToChange = new PackageId(fields[0], fields[1], fields[2]);
                        newVersion = fields[3];
                    } else {
                        throw new IllegalArgumentException(
                                "expected 4 fields separated by ':', was= '" + s + "'");
                    }
                    changeArtifact = false;
                } else if (REVERSE.equals(s)) {
                    reverse = true;
                } else if (NO_DEPENCENCIES.equals(s)) {
                    noDependencies = true;
                } else if (MODULE.equals(s)) {
                    module = true;
                } else if (DEPENDENCY.equals(s)) {
                    dependency = true;
                } else if (BACKUP_COPY.equals(s)) {
                    makeBackupCopy = true;
                } else if (CHANGE_ARTIFACT.equals(s)) {
                    changeArtifact = true;
                } else if (FULL_STACKTRACE.equals(s)) {
                    fullStacktrace = true;
                } else if (OMIT_NULL_VERSION.equals(s)) {
                    omitNullVersion = true;
                } else {
                    paths.add(s);
                }
            }
        }
        if (artifactToChange != null && dependencyRegexp != null) {
            throw new IllegalArgumentException(
                    "change artifact (-c) cannot be mixed with dependency filter (-d)");
        }
    }

    public static String getUsage() {
        return USAGE;
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

    public PackageId getArtifactToChange() {
        return artifactToChange;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public boolean isMakeBackupCopy() {
        return makeBackupCopy;
    }

    public boolean isFullStacktrace() {
        return fullStacktrace;
    }

    public boolean isOmitNullVersion() {
        return omitNullVersion;
    }

    @Override
    public String toString() {
        return "configuration:" +
                (reverse ? "\nreverse=" + reverse : "") +
                (noDependencies ? "\no dependencies=" + noDependencies : "") +
                (moduleRegexp != null ? "\nmodule regexp=" + moduleRegexp : "") +
                (dependencyRegexp != null ? "\ndependency regexp=" + dependencyRegexp : "") +
                (omitNullVersion ? "\nomit null version=" + omitNullVersion : "") +
                (artifactToChange != null ? "\nartifact to change=" + artifactToChange : "") +
                (newVersion != null ? "\nnew version=" + newVersion : "") +
                (makeBackupCopy ? "\nmake backup copy=" + makeBackupCopy : "") +
                "\npaths=" + paths.toString();
    }


}
