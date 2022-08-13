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
	
2. Get projects depending on `jupiter` (all versions):
	```	
	./run-maven-dependency-list.sh /home/fra/Devel/Code -d jupiter -r 
	```
	
3. Get projects depending on `jupiter` version 5.6.0:
	```	
	./run-maven-dependency-list.sh /home/fra/Devel/Code -d jupiter\.*5\\.6\\.0 -r 
	```

4. Get projects from `fillumina` depending on `jupiter`  5.6.0:
	```
	./run-maven-dependency-list.sh /home/fra/Devel/Code -d jupiter\.*5\\.6\\.0 -r -m fillumina
	```


## Creating an executable script for *unix

To create an executable script use the useful [gist](https://gist.github.com/briandealwis/782862/9cc9ef8a78af3bb78a692313f8bfa6fb76ab4663) which has been copied (with a reference added) to the project root. After compiling the code execute the script `run-script-creator.sh` specifying as parameter the output script name.
i.e:
```
 ./run-script-creator.sh run-maven-dependency.sh  
```


