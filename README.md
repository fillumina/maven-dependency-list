# maven-dependency-list

It **browses** trees of java `maven` projects (`pom.xml`) listing the dependencies for each project with various filters and eventually **changes** (filtered by project) the version of a specified dependency or plugin.

Java projects usually depends on a lot of reusable packages which increases reusability, testing and promotes single-responsibility principle ([SRP](https://en.wikipedia.org/wiki/Single-responsibility_principle)).

On the back side if a package is used in many different projects it rapidly becomes difficult to track what version each project is using (especially if they are a lot!).

This utility has been created to show who depends on what in trees of projects. It can view dependencies or dependent packages and change the version of a dependency in bulk (i.e. when removing or adding [SNAPSHOT](https://maven.apache.org/guides/getting-started/index.html#what-is-a-snapshot-version) before or after releasing a project version).

## Build

Use  `run-script-creator.sh` to create a `java` command line application embedded into a shell script (`run-maven-dependency-list.sh`). It needs a compatible [JRE 8](https://www.java.com/en/download/manual.jsp) available in the system. If you don't have access to `bash` shell you can call it directly with: `java -jar maven-dependency-list-1.2.jar`  where the `jar` file is created in the `target` folder after compilation (`mvn clean install`).

## Tree visualization

This application is geared towards directory of java projects with useful features such as reverse searching (dependent object for each dependency), to analyze a single project use the [Maven Dependency Tree Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/tree-mojo.html).

## Versions

- **1.2.2** 19/11/23 fix implicit or inherited `groupId` and `version`

- **1.2.1** 15/11/22 algorithm fix (corner case)

- **1.2** 15/11/22 adds the possibility to change the version of a dependency/plugin

- **1.1** 20/08/22 various fixes

- **1.0** 13/08/22 first version

## Creating an executable script for *unix

To create an executable script use the useful [gist](https://gist.github.com/briandealwis/782862/9cc9ef8a78af3bb78a692313f8bfa6fb76ab4663) which has been adapted and copied (with a reference added) to the project root. After compiling the code execute the script `run-script-creator.sh` to generate the final script `run-maven-dependency-list`.

## ## Options

It accepts the following parameters:

- `-h` or `--help` print an help message

- `-r` lists dependencies by dependent projects

- `-n` lists only main projects found in the given directories

- `-p project-regexp` specifies a regexp filter for main project names ($ and ^ will be added)

- `-d dependency-regexp` specifies a regexp filter for dependency names ($ and ^ will be added)

- `-c group:artifact:ver:new-ver` change version of all package occurrences  found within the tree hierarchy honoring the project filtering (`-p`).
  It doesn't support dependency filter (`-d`).

- `-b` make a backup copy of the changed `pom.xml` -> `pom.xml.bak` (only with `-c`)

- `-j` print a full java exception stacktrace on error (for debugging)

- It accepts any number of directories that will be traversed searching for sub-projects (a directory containing a `pom.xml` file).

## Examples

Assuming `run-maven-dependency-list.sh` as the chosen script name.

1. Get all projects depending on packages witha a  `SNAPSHOT` version:
   
   ```
   run-maven-dependency-list.sh . -d SNAPSHOT
   ```
   
   ```
   arguments passed:
   dependency regexp=^.*SNAPSHOT.*$
   paths=[.]
   
   dependencies use by project
   
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
   arguments passed:
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
   arguments passed:
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
   arguments passed:
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
   
   dependencies use by project
   
   searching in: .
   
   modified artifact in ./amzbridgems/pom.xml
   modified artifact in ./inventoryms/pom.xml
   ```
