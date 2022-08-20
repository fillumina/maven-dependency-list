#!/bin/bash

cat script-creator.txt target/*.jar > run-maven-dependency-list.sh
chmod +x run-maven-dependency-list.sh

echo "file run-maven-dependency-list.sh created successfully"
