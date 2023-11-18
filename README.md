# maven-dependency-list

It **browses** trees of java `maven` projects (`pom.xml`) listing the dependencies for each project with various filters and **changes** all occurrences (eventually filtered by module) of the version of a specified dependency or plugin.

It is a `java` command line application embedded into a unix shell script (`run-maven-dependency-list.sh`) and needs a compatible [JRE 8](https://www.java.com/en/download/manual.jsp) available in the system. If you don't have access to a unix shell (`bash`) you can call it directly with: `java -jar maven-dependency-list-1.2.jar`  where the `jar` file is created in the `target` folder after compilation (`mvn clean install`).

## Tree visualization

This application is geared towards directory of java projects with useful features such as reverse searching (dependent object for each dependency), to analyze a single project use the [Maven Dependency Tree Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/tree-mojo.html).

## Versions

- **1.2.2** fix implicit or inherited groupId and version

- **1.2.1** 15/11/22 indexOf algorithm fix (corner case)

- **1.2** 15/11/22 adds the possibility to change the version of a dependency/plugin

- **1.1** 20/08/22 various fixes

- **1.0** 13/08/22 first version

## Creating an executable script for *unix

To create an executable script use the useful [gist](https://gist.github.com/briandealwis/782862/9cc9ef8a78af3bb78a692313f8bfa6fb76ab4663) which has been adapted and copied (with a reference added) to the project root. After compiling the code execute the script `run-script-creator.sh` to generate the final script `run-maven-dependency-list`.

## Scenario

I like to build my projects around a lot of different reusable packages to increase reusability, testing and promoting single-responsibility principle ([SRP](https://en.wikipedia.org/wiki/Single-responsibility_principle)).

This takes as a drawback a difficulty in managing versions: if a package is used in many different projects it is difficult to track what version each project is using (especially if they are a lot!). Moreover I want my build to be replicable so package version should stay consistent with the code (never change a code once the version has been published).

This little utility has been created to help manage that: it searches dependencies along many different projects and is able to change the version of a dependency in bulk (i.e. when removing or adding [SNAPSHOT](https://maven.apache.org/guides/getting-started/index.html#what-is-a-snapshot-version) before or after releasing a project version).

## Options

It accepts the following parameters:

- `-h` or `--help` print an help message

- `-r` lists owning module for each dependency instead of the dependencies for each module.

- `-n` lists only modules without dependencies

- `-m module-regexp` specifies a regexp filter for mudule names ($ and ^ will be added)

- `-d dependency-regexp` specifies a regexp filter for dependency names ($ and ^ will be added)

- `-v` omit dependencies or plugins with no version specified

- `-c group:artifact:ver:new-ver` change version of all package occurences  found within the tree hierarchy honoring the module filtering (`-m`).
  It doesn't support dependency filter (`-d`).

- `-b` make a backup copy of the changed `pom.xml` -> `pom.xml.bak` (only with `-c`)

- `-j` pring a full java exception stacktrace on error

- It accepts any number of directories that will be traversed searching for sub-projects.

## Examples

Assuming `run-maven-dependency-list.sh` as the chosen script name.

1. Get all projects with `SNAPSHOT` dependencies:
   
   ```
   run-maven-dependency-list.sh /home/fra/Devel/Code -d
    SNAPSHOT
   ```
   
   ```
   arguments passed:
   dependency regexp=^.*SNAPSHOT.*$
   paths=[/home/fra/Devel/Code]
   
   dependencies use by module
   
   searching in: /home/fra/Devel/Code
   
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
   run-maven-dependency-list.sh /home/fra/Devel/Code -d jupiter -r
   ```
   
   ```
   arguments passed:
   reverse=true
   dependency regexp=^.*jupiter.*$
   paths=[/home/fra/Devel/Code]
   
   modules using dependencies
   
   searching in: /home/fra/Devel/Code
   
   org.junit.jupiter:junit-jupiter-engine
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
   run-maven-dependency-list.sh /home/fra/Devel/Code -d jupiter\.*5\\.6\\.0 -r
   ```
   
   ```
   arguments passed:
   reverse=true
   module regexp=^.*fillumina.*$
   dependency regexp=^.*jupiter.*5\.6\.0.*$
   paths=[/home/fra/Devel/Code]
   
   modules using dependencies
   
   searching in: /home/fra/Devel/Code
   
   org.junit.jupiter:junit-jupiter-params:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-api:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   
   org.junit.jupiter:junit-jupiter-engine:5.6.0
           com.fillumina:collections:1.0.1-SNAPSHOT
           com.fillumina:formio-gen:1.0-SNAPSHOT
   ```

4. Get projects from modules containing `fillumina` depending on `jupiter`  5.6.0:
   
   ```
   run-maven-dependency-list.sh /home/fra/Devel/Code -d jupiter\.*5\\.6\\.0 -r -m fillumina
   ```
   
   ```
   arguments passed:
   reverse=true
   module regexp=^.*fillumina.*$
   dependency regexp=^.*jupiter.*5\.6\.0.*$
   paths=[/home/fra/Devel/Code]
   
   modules using dependencies
   
   searching in: /home/fra/Devel/Code
   
   org.junit.jupiter:junit-jupiter-params:5.6.0
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
   run-maven-dependency-list.sh -m fillumina -c javax.validation:validation-api:2.0.1.Final:3.0.0 .
   ```
   
   ```
   module regexp=^.*fillumina.*$
   artifact to change=javax.validation:validation-api:2.0.1.Final
   new version=3.0.0
   paths=[.]
   
   dependencies use by module
   
   searching in: .
   
   modified artifact in ./amzbridgems/pom.xml
   modified artifact in ./inventoryms/pom.xml
   ```
