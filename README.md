# maven-reverse-dependency

It browses trees of java `maven` projects indexing dependencies.

It accepts the following parameters:

- `-r` lists owning module for each dependency instead of the dependencies for each module.

- `-m module-regexp` specifies a regexp filter for mudule names

- `-d dependency-regexp` specifies a regexp filter for dependency names

- It accepts any number of directories that will be traversed searching for sub-projects.

## Creating an executable script for *unix

To create an executable script use the useful [gist](https://gist.github.com/briandealwis/782862/9cc9ef8a78af3bb78a692313f8bfa6fb76ab4663) which has been copied (with a reference added) to the project root. After compiling the code execute the script `run-script-creator.sh` specifying as parameter the output script name.
i.e:
```
 ./run-script-creator.sh run-maven-dependency.sh  
```


