# maven-dependency-list

**Scans** trees of java `maven` projects listing the dependencies of each project found. It allows **selections** by using REGEXP filtering and can **change** the version of a dependency or plugin in all projects at once. It also allows reverse viewing which list each project depending on a specific dependency.

## Build

The Java project can be built with the usual `mvn clean install` given the presence in the path of both a JDK 21+ and maven.

Use the script  `run-script-creator.sh` (derived from this [gist](https://gist.github.com/briandealwis/782862/9cc9ef8a78af3bb78a692313f8bfa6fb76ab4663)) to create a `java` command line application embedded into a shell script (`run-maven-dependency-list.sh`). It needs a compatible JRE 21 available in the system.

If you don't have access to a `bash` shell you can use the application by calling it directly with: `java -jar maven-dependency-list-1.3.0.jar`  where the `jar` file is created in the `target` folder after compilation (`mvn clean install`).

## Tree Visualization

This application is geared towards directory of java projects with useful features such as reverse searching (showing dependent projects for each dependency). To analyze a single project and view the full dependency tree use [Maven Dependency Tree Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/tree-mojo.html).

## Versions

- **1.3.0** 27/09/26
  - require JDK 21+, JUnit 6 and the current build plugins
  - stop a rewritten `pom.xml` being corrupted when the new version is shorter than the old one
  - report a version that cannot be resolved instead of quietly using the project own version
  - resolve a version from a parent pom: on disk through `<relativePath>`, and from a local
    maven repository given with `-m`, never trusting a file that is not the parent the pom names
  - resolve `${parent.version}`, `${project.parent.version}` and the two groupId spellings
  - `-c` pins a version whose property comes from a parent, so one project moves and its
    siblings do not, and warns about a pom that declares the artifact it was pointed at but did
    not change
  - `-o group:artifact:ver` reports the projects still behind on an artifact
  - `-a` looks inside the folders that only group projects
  - every run says how much of the tree it read, and steps over a folder it cannot read instead
    of ending the run
  - order versions the way Maven does, held to it by a test that runs Maven own comparator
  - report a mistyped command line in one line, and a path that cannot be read as an error
  - say in the help what each option does, and what the tool does not do
  - fix the `scm` connection

- **1.2.4** 27/09/26 fix corrupted `pom.xml` when a version change shortens the file, fix errors with `-n -v` and `-n -r`, require JDK 11+

- **1.2.3** 21/11/23 fix implicit `org.apache.maven.plugins` as `groupId`  for plugins

- **1.2.2** 19/11/23 fix implicit or inherited `groupId` and `version`

- **1.2.1** 15/11/22 algorithm fix (corner case)

- **1.2** 15/11/22 adds the possibility to change the version of a dependency/plugin

- **1.1** 20/08/22 various fixes

- **1.0** 13/08/22 first version

## Options

It accepts the following parameters:

- `-h` or `--help` print an help message

- `-r` lists dependencies by dependent projects

- `-n` lists only main projects found in the given directories

- `-p project-regexp` specifies a regexp filter for main project names ($ and ^ will be added)

- `-d dependency-regexp` specifies a regexp filter for dependency names ($ and ^ will be added)

- `-c group:artifact:ver:new-ver` change version of all package occurrences  found within the tree hierarchy honoring the project filtering (`-p`).
  It doesn't support dependency filter (`-d`). A version inherited from a parent is pinned in the file that declares it, so one project moves without its siblings.

- `-b` make a backup copy of the changed `pom.xml` -> `pom.xml.bak` (only with `-c`)

- `-j` print a full java exception stacktrace on error (for debugging). It covers failures while reading and rewriting the poms, not a malformed command line, which is always reported in one line

- `-v` omit dependencies/plugins with null version

- `-m folder` read the properties of a parent from a local maven repository in that folder, which is where a parent with an empty `<relativePath/>` lives. Only what is named here is consulted, and it has to exist already. The usual value is `~/.m2/repository`

- `-a` also look inside folders that have no `pom.xml` of their own, for a tree whose projects are grouped under plain folders. A `target`, `.git` or `src` folder is still left alone, and only in this mode, so nothing the default already reads is affected. A `pom.xml` under `src` is a resource someone ships, not a project, which is what a real tree turned up

- `-o group:artifact:ver` keep only the projects still on an **older** version of that artifact. The version you pass is the one you consider current: `-o org.acme:lib:4.2` reports every project still on 3.1 or 4.2-SNAPSHOT and leaves the ones already on 4.2 or newer out. A dependency whose version could not be resolved is always kept, because nothing can say it is not behind. It cannot be mixed with `-c`, and with `-n` it keeps nothing, since `-n` reports projects with no dependencies at all

- It accepts any number of directories that will be traversed searching for sub-projects (a directory containing a `pom.xml` file).

## Limitations

This tool reads the text of each `pom.xml`; it is not a Maven model. It does not:

- consult anything outside the files on disk and the repository named with `-m`. A `<parent>` is followed through its `<relativePath>`, and the file is only trusted when its groupId, artifactId and version are the ones the child declared, so an unrelated pom that happens to sit at `../pom.xml` is ignored. A `dependencyManagement`, an imported BOM and a profile are never read;
- invent a version. A version written as `${property}` that cannot be resolved keeps its placeholder in the listing and is named on `stderr`. `-v` does not hide it, because it is not a version that was never declared, it is one that could not be read;
- resolve transitive dependencies, the local repository or version conflicts. For a single project's real tree use the [Maven Dependency Tree Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/tree-mojo.html);
- tell a `<dependency>` inside `<dependencyManagement>` from a declared one, so `-c` rewrites a managed version exactly as it rewrites a declared one;
- enter a directory that has no `pom.xml`. The given folder is always entered, and so is every folder that holds a `pom.xml` of its own, but a folder that merely groups projects is not, unless `-a` is given: give each of them its own path on the command line, give the grouping folder a `pom.xml` of its own, or pass `-a`. A tree of projects grouped under plain folders is a normal shape and the default will read none of it, which the coverage line says outright rather than leaving an empty listing to be guessed at;
- read anything but `pom.xml`, so `build.gradle`, `*.gradle.kts` and ivy files are out of reach.

Three details that are easy to trip over:

- the `-p` and `-d` regexps are wrapped as `^.*<regexp>.*$`, so an unanchored regexp always matches as a substring;
- a version written as a single `${property}` is rewritten in one of two ways. If the property is declared in the same file and used once, `-c` changes the property's value, which moves everything that shares it. If the property comes from a parent, `-c` cannot change it here, so it pins the version in this file instead: `${lib.version}` becomes `<version>4.2</version>`. That moves one project and leaves its siblings and the parent alone;
- an unparsable `pom.xml` aborts the whole run, not just that project.

## Output and exit codes

Output is plain text on `stdout`: one `group:artifact:version` per line, with a project's dependencies indented by a tab. A dependency with no version prints as `group:artifact`. The format is meant to be read or piped into `grep` and `awk`.

Every run prints a `configuration:` block and a `searching in:` line before the listing, so a script that wants only the data has to skip them, and one line on `stderr` at the end saying how many `pom.xml` files were read and how many entries were listed. The two numbers are usually different: a project that declares no dependencies gets no line of its own unless `-n` is used, which is why both are printed. A run that read no `pom.xml` at all says so in a `WARNING:` and points at `-a`, since a tree whose projects are grouped under plain folders is the usual reason, and an empty listing on its own does not say so. Two things add a `WARNING:` line on `stderr`, which never mixes into the data on `stdout`: a version that could not be resolved, and a pom that declares the artifact `-c` was pointed at but did not change, which is how a sweep tells you which projects it did not move.

| Situation | What is printed | Exit code |
| --- | --- | --- |
| normal run | the listing on `stdout` | 0 |
| `-h`, or no arguments at all | the usage text on `stdout` | 0 |
| a `pom.xml` that cannot be read or parsed | `ERROR: <message>` on `stderr` | 1 |
| the same, with `-j` | the full Java stack trace on `stderr` | 1 |
| a malformed command line, such as `-c` without four fields or an invalid regexp | one `ERROR:` block on `stderr`, never a stack trace. A bad regexp repeats the offending pattern over a few lines | 1 |
| a path that does not exist or cannot be read | `ERROR: cannot read <path>` on `stderr` | 1 |

## Examples

Assuming `run-maven-dependency-list.sh` as the chosen script name.

1. Get all projects depending on packages witha a  `SNAPSHOT` version:
   
   ```
   run-maven-dependency-list.sh . -d SNAPSHOT
   ```
   
   ```
   configuration:
   dependency regexp=^.*SNAPSHOT.*$
   paths=[.]
   
   dependencies used by project
   
   searching in: .
   
   com.fillumina:performance-tools-multi:2.0-SNAPSHOT
           com.fillumina:performance-tools:2.0-SNAPSHOT
   
   com.mycompany:xmi-to-jdl:1.0-SNAPSHOT
           com.fillumina:xmi-to-jdl:1.0-SNAPSHOT
   
   com.fillumina.emporia:emporia:0.0.1-SNAPSHOT
           com.fillumina:dataimport:1.0-SNAPSHOT
   
   com.fillumina:lcs-algorithms:1.0-SNAPSHOT
           com.fillumina:performance-tools:1.2-SNAPSHOT
   
   com.fillumina:java-utils-main:1.0-SNAPSHOT
           com.fillumina:performance-tools:0.1-SNAPSHOT
           com.fillumina:java-utils:1.0-SNAPSHOT
           com.fillumina:performance-tools-junit:0.1-SNAPSHOT
   
   com.fillumina:lcs:1.0-SNAPSHOT
           com.fillumina:performance-tools-junit:1.2-SNAPSHOT
   
   com.fillumina:bean-tools-main:1.0-SNAPSHOT
           com.fillumina:performance-tools:1.2-SNAPSHOT
           com.fillumina:performance-tools-junit:1.2-SNAPSHOT
           com.fillumina:bean-tools:1.0-SNAPSHOT
   
   com.fillumina:whatsapp-intelli-cleaner:1.0-SNAPSHOT
           com.fillumina:performance-tools:2.0-SNAPSHOT
   
   com.fillumina:lcs-test-util:1.0-SNAPSHOT
           com.fillumina:performance-tools:1.2-SNAPSHOT
   ```

2. Get projects depending on `jupiter` (all versions):
   
   ```
   run-maven-dependency-list.sh . -d jupiter -r
   ```
   
   ```
   configuration:
   reverse=true
   dependency regexp=^.*jupiter.*$
   paths=[.]
   
   projects using dependencies
   
   searching in: /home/fra/Devel/Cod.:junit-jupiter-engine
           com.fillumina.emporia:emporia:0.0.1-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-params:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-params:5.4.2
           com.fillumina:whatsapp-intelli-cleaner:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-api:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-api:5.4.2
           com.fillumina:whatsapp-intelli-cleaner:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-params:5.3.1
           com.fillumina:xmi-to-jdl:2.0-SNAPSHOT
           com.fillumina:fotostand-configurator:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-engine:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-engine:5.4.2
           com.fillumina:whatsapp-intelli-cleaner:1.0-SNAPSHOT
   ```

3. Get projects depending on `jupiter` version 5.6.0:
   
   ```
   run-maven-dependency-list.sh . -d jupiter\.*5\\.6\\.0 -r
   ```
   
   ```
   configuration:
   reverse=true
   project regexp=^.*fillumina.*$
   dependency regexp=^.*jupiter.*5\.6\.0.*$
   paths=[.]
   
   projects using dependencies
   
   searching in: /home/fra/Devel/Cod.:junit-jupiter-params:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-api:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-engine:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   ```

4. Get projects from projects containing `fillumina` depending on `jupiter`  5.6.0:
   
   ```
   run-maven-dependency-list.sh . -d jupiter\.*5\\.6\\.0 -r -p fillumina
   ```
   
   ```
   configuration:
   reverse=true
   project regexp=^.*fillumina.*$
   dependency regexp=^.*jupiter.*5\.6\.0.*$
   paths=[.]
   
   projects using dependencies
   
   searching in: /home/fra/Devel/Cod.:junit-jupiter-params:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-api:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-engine:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   ```

5. Change the version of the specified dependency from 2.0.1 to 3.0.0 in all projects having `fillumina` as part of the artifact-id or group-id:
   
   ```
   run-maven-dependency-list.sh -p fillumina -c javax.validation:validation-api:2.0.1.Final:3.0.0 .
   ```
   
   ```
   project regexp=^.*fillumina.*$
   artifact to change=javax.validation:validation-api:2.0.1.Final
   new version=3.0.0
   paths=[.]
   
   dependencies used by project
   
   searching in: .
   
   modified artifact in ./amzbridgems/pom.xml
   modified artifact in ./inventoryms/pom.xml
   ```
