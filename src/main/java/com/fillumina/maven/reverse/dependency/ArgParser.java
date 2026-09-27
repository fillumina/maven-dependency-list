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
    private static final String ALL_FOLDERS = "-a";

    /** What to tell someone whose run read nothing, since that is usually why. */
    static final String ALL_FOLDERS_HINT = "-" + ALL_FOLDERS.substring(1);

    private static final String[] USAGE = {
        "maven-dependency-list " + VERSION + " (" + VERSION_DATA + ")",
        "Francesco Illuminati fillumina@gmail.com - https://github.com/fillumina/maven-dependency-list",
        "",
        "Lists the versions the projects in a tree of folders are on, and changes them.",
        "",
        "usage: maven-dependency-list [options] <folder> [<folder>...]",
        "",
        "  Each folder is searched for pom.xml. The folder you name is always entered, and",
        "  so is every folder that has a pom.xml of its own. A folder that only groups",
        "  projects is not entered unless " + ALL_FOLDERS + " is given.",
        "",
        "what it shows",
        "  " + REVERSE + "  list by dependency instead of by project: each dependency",
        "        on top, the projects using it indented under it",
        "  " + NO_DEPENCENCIES + "  list the projects only, without what they depend on",
        "  " + OUTDATED + " group:artifact:ver",
        "        keep only the dependencies of that artifact that are on an OLDER",
        "        version than the one given, which is how a tree is asked which",
        "        projects are behind. A version that could not be resolved is kept,",
        "        because nothing can say it is not behind. Cannot be used with " + CHANGE_ARTIFACT,
        "",
        "what it looks at",
        "  " + PROJECT + " regexp   only the projects whose group:artifact:version matches",
        "  " + DEPENDENCY + " regexp   only the dependencies and plugins that match",
        "        both are matched as a substring: what you give is wrapped in ^.* and",
        "        .*$, so an anchor of your own will not match anything",
        "  " + OMIT_NULL_VERSION + "  leave out the dependencies and plugins with no version",
        "  " + ALL_FOLDERS + " also look inside folders that have no pom.xml of their own, for a",
        "        tree whose projects are grouped under plain folders. A target, .git or",
        "        src folder is still left alone, and a folder that cannot be read is",
        "        reported and stepped over rather than ending the run",
        "",
        "what it changes",
        "  " + CHANGE_ARTIFACT + " group:artifact:ver:new",
        "        set every occurrence of that artifact at that version to the new one",
        "        a version written as a property is changed where it is declared when",
        "        that is this pom, and pinned in place when the property comes from a",
        "        parent, so one project moves and its siblings do not",
        "        cannot be used with " + DEPENDENCY + " or " + OUTDATED + "; " + PROJECT + " still applies",
        "  " + BACKUP_COPY + "  with " + CHANGE_ARTIFACT + ", move each changed pom.xml to",
        "        pom.xml.bak before writing the new one, replacing an older .bak",
        "",
        "what else",
        "  " + HELP_SHORT + ", " + HELP_LONG + "  print this",
        "  " + FULL_STACKTRACE + "  print the whole java stacktrace on error. It covers the poms,",
        "        not a mistyped argument, which is always reported in one line",
        "",
        "what comes out",
        "  Plain text on stdout, one group:artifact:version per line, the dependencies",
        "  of a project indented under it. A version that could not be resolved, a",
        "  pom.xml that could not be read, a folder that had to be stepped over, and a",
        "  pom.xml that declares the artifact " + CHANGE_ARTIFACT + " was pointed at but did not",
        "  change, all say so on stderr.",
        "  Exit code 0 when it ran, 1 when it did not.",
    };

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
    private boolean allFolders;
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
                } else if (ALL_FOLDERS.equals(s)) {
                    allFolders = true;
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
        return String.join(System.lineSeparator(), USAGE);
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
     * Whether to look inside folders that have no pom.xml of their own, which is
     * what a tree of projects grouped under plain folders needs.
     */
    public boolean isAllFolders() {
        return allFolders;
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
                (allFolders ? "\nall folders=" + allFolders : "") +
                (outdatedArtifact != null ? "\noutdated artifact=" + outdatedArtifact : "") +
                (artifactToChange != null ? "\nartifact to change=" + artifactToChange : "") +
                (newVersion != null ? "\nnew version=" + newVersion : "") +
                (makeBackupCopy ? "\nmake backup copy=" + makeBackupCopy : "") +
                "\npaths=" + paths.toString();
    }


}
