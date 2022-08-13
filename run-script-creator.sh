#!/bin/bash

if [[ "$1" == "" ]]; then
  echo "use: $0 script-name"
  exit 0
fi

cat script-creator.txt target/*.jar > $1
chmod +x $1

echo "file $1 created successfully"
