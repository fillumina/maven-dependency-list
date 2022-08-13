# maven-reverse-dependency

It browses trees of java `maven` projects listing the dependencies for each project with various filters.

It accepts the following parameters:

- `-r` lists owning module for each dependency instead of the dependencies for each module.

- `-m module-regexp` specifies a regexp filter for mudule names

- `-d dependency-regexp` specifies a regexp filter for dependency names

- It accepts any number of directories that will be traversed searching for sub-projects.

## Examples

Assuming `run-maven-dependency-list.sh` as the chosen script name.

1. Get all projects with `SNAPSHOT` dependencies:

	```
	./run-maven-dependency-list.sh /home/fra/Devel/Code -d
	 SNAPSHOT 
	```
	
	```
	arguments passed:
	dependency regexp=^.*SNAPSHOT.*$
	paths=[/home/fra/Devel/Code]
	
	dependencies use by module
	
	searching in: /home/fra/Devel/Code
	
	com.fillumina:performance-tools-multi:2.0-SNAPSHOT
	        ${project.groupId}:performance-tools:2.0-SNAPSHOT
	
	com.mycompany:xmi-to-emporia-jdl:1.0-SNAPSHOT
	        com.fillumina:xmi-to-jdl:1.0-SNAPSHOT
	
	com.fillumina.emporia:emporia:0.0.1-SNAPSHOT
	        com.fillumina:dataimport:1.0-SNAPSHOT
	
	com.fillumina:lcs-algorithms:1.0-SNAPSHOT
	        ${project.groupId}:performance-tools:1.2-SNAPSHOT
		
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
	        ${project.groupId}:performance-tools:2.0-SNAPSHOT
	
	com.fillumina:lcs-test-util:1.0-SNAPSHOT
	        ${project.groupId}:performance-tools:1.2-SNAPSHOT
		
	```
2. Get projects depending on `jupiter` (all versions):
	```	
	./run-maven-dependency-list.sh /home/fra/Devel/Code -d jupiter -r 
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
	./run-maven-dependency-list.sh /home/fra/Devel/Code -d jupiter\.*5\\.6\\.0 -r 
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

4. Get projects from `fillumina` depending on `jupiter`  5.6.0:
	```
	./run-maven-dependency-list.sh /home/fra/Devel/Code -d jupiter\.*5\\.6\\.0 -r -m fillumina
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
## Creating an executable script for *unix

To create an executable script use the useful [gist](https://gist.github.com/briandealwis/782862/9cc9ef8a78af3bb78a692313f8bfa6fb76ab4663) which has been copied (with a reference added) to the project root. After compiling the code execute the script `run-script-creator.sh` specifying as parameter the output script name.
i.e:
```
 ./run-script-creator.sh run-maven-dependency.sh  
```


