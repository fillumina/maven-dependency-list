# maven-dependency-list

**Scans** trees of java `maven` projects listing the dependencies of each project found. It allows **selections** by using REGEXP filtering and can **change** the version of a dependency or plugin in all projects at once. It also allows reverse viewing which list each project depending on a specific dependency.

## Build

The Java project can be built with the usual `mvn clean install` given the presence in the path of both a JDK 1.8+ and maven.

Use the script  `run-script-creator.sh` (derived from this [gist](https://gist.github.com/briandealwis/782862/9cc9ef8a78af3bb78a692313f8bfa6fb76ab4663)) to create a `java` command line application embedded into a shell script (`run-maven-dependency-list.sh`). It needs a compatible [JRE 8](https://www.java.com/en/download/manual.jsp) available in the system.

If you don't have access to a `bash` shell you can use the application by calling it directly with: `java -jar maven-dependency-list-1.2.jar`  where the `jar` file is created in the `target` folder after compilation (`mvn clean install`).

## Tree Visualization

This application is geared towards directory of java projects with useful features such as reverse searching (showing dependent projects for each dependency). To analyze a single project and view the full dependency tree use [Maven Dependency Tree Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/tree-mojo.html).

## Versions

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
