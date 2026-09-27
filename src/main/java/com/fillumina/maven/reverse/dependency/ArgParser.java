package com.fillumina.maven.reverse.dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ArgParser {
    private static final String VERSION = "1.3.0";
    private static final String VERSION_DATA = "27/09/26";

    private static final String HELP_LONG = "--help";
    private static final String HELP_SHORT = "-h";
    private static final String FULL_STACKTRACE = "-j";
    private static final String CHANGE_ARTIFACT = "-c";
    private static final String BACKUP_COPY = "-b";
    private static final String DEPENDENCY = "-d";
    private static final String PROJECT = "-p";
    private static final String NO_DEPENCENCIES = "-n";
    private static final String REVERSE = "-r";
    private static final String OMIT_NULL_VERSION = "-v";
    private static final String OUTDATED = "-o";

    private static final String USAGE =
        "by Francesco Illuminati fillumina@gmail.com - https://github.com/fillumina/maven-dependency-list " +
            "- ver " + VERSION + " " + VERSION_DATA +"\n" +
        "List and change package versions in a directory tree of maven pom.xml with filters.\n" +
        "options: [" + HELP_SHORT + "|" + HELP_LONG + "] [" + REVERSE + "] [" + NO_DEPENCENCIES +"] [" + PROJECT + " project_regexp] " +
            "[" + DEPENDENCY + " dependecy_regexp] [" + CHANGE_ARTIFACT + " group:artifact:ver:new-ver] [" + OUTDATED + " group:artifact:ver] " +
            "[" + BACKUP_COPY + "] paths...\n" +
        "where:\n" +
        HELP_SHORT + " or " + HELP_LONG + " print this help\n" +
        REVERSE + " print dependent projects by dependencies\n" +
        NO_DEPENCENCIES + " print only project names without dependencies\n" +
        PROJECT + " regexp set a project filter\n" +
        DEPENDENCY + " regexp set a dependency/plugin filter\n" +
        OMIT_NULL_VERSION + " omit dependencies/plugins with null version\n" +
        CHANGE_ARTIFACT + " group:artifact:ver:new-ver change version of all package occurences\n" +
        OUTDATED + " group:artifact:ver keep only the projects still on an older version of that\n" +
        "   artifact, and cannot be mixed with " + CHANGE_ARTIFACT + "\n" +
        "   cannot be mixed with dependency filter (" + DEPENDENCY + "), can use project filtering (" + PROJECT + ")\n" +
        BACKUP_COPY + " make a backup copy of the changed pom.xml as pom.xml.bak (only with " + CHANGE_ARTIFACT + ")\n" +
        FULL_STACKTRACE + " print a full java exception stacktrace\n" +
        "paths... path list to search for pom.xml\n";

    private boolean help;
    private boolean error;
    private boolean reverse;
    private boolean noDependencies;
    private Pattern projectRegexp;
    private Pattern dependencyRegexp;
    private List<String> paths = new ArrayList<>();
    private PackageId artifactToChange;
    private String newVersion;
    private boolean makeBackupCopy;
    private boolean fullStacktrace;
    private boolean omitNullVersion;
    private PackageId outdatedArtifact;

    public ArgParser(String[] args) {
        boolean project = false, dependency = false, changeArtifact = false, outdated = false;
        if (args == null || args.length == 0) {
            error = true;
        } else {
            for (String s : args) {
                if (HELP_SHORT.equals(s) || HELP_LONG.equals(s)) {
                    help = true;
                } else if (project) {
                    projectRegexp = Pattern.compile("^.*" + s + ".*$");
                    project = false;
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
                } else if (outdated) {
                    String[] fields = s.split(":");
                    if (fields.length != 3) {
                        throw new IllegalArgumentException(
                                "expected 3 fields separated by ':', was= '" + s + "'");
                    }
                    outdatedArtifact = new PackageId(fields[0], fields[1], fields[2]);
                    outdated = false;
                } else if (REVERSE.equals(s)) {
                    reverse = true;
                } else if (NO_DEPENCENCIES.equals(s)) {
                    noDependencies = true;
                } else if (PROJECT.equals(s)) {
                    project = true;
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
                } else if (OUTDATED.equals(s)) {
                    outdated = true;
                } else {
                    paths.add(s);
                }
            }
        }
        if (artifactToChange != null && dependencyRegexp != null) {
            throw new IllegalArgumentException(
                    "change artifact (-c) cannot be mixed with dependency filter (-d)");
        }
        if (artifactToChange != null && outdatedArtifact != null) {
            throw new IllegalArgumentException(
                    "change artifact (-c) cannot be mixed with outdated artifact (-o)");
        }
    }

    public static String getUsage() {
        return USAGE;
    }

    public boolean isReverse() {
        return reverse;
    }

    public Pattern getModuleRegexp() {
        return projectRegexp;
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

    /**
     * The artifact to report on, or null when every dependency should be shown.
     */
    public PackageId getOutdatedArtifact() {
        return outdatedArtifact;
    }

    @Override
    public String toString() {
        return "configuration:" +
                (reverse ? "\nreverse=" + reverse : "") +
                (noDependencies ? "\no dependencies=" + noDependencies : "") +
                (projectRegexp != null ? "\nproject regexp=" + projectRegexp : "") +
                (dependencyRegexp != null ? "\ndependency regexp=" + dependencyRegexp : "") +
                (omitNullVersion ? "\nomit null version=" + omitNullVersion : "") +
                (outdatedArtifact != null ? "\noutdated artifact=" + outdatedArtifact : "") +
                (artifactToChange != null ? "\nartifact to change=" + artifactToChange : "") +
                (newVersion != null ? "\nnew version=" + newVersion : "") +
                (makeBackupCopy ? "\nmake backup copy=" + makeBackupCopy : "") +
                "\npaths=" + paths.toString();
    }


}
