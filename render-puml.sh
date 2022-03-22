#!/usr/bin/bash
set -e
set -x

SCRIPT_DIR=$(dirname $(realpath $0))
PLANTUML_JAR_LINK="https://github.com/plantuml/plantuml/releases/download/v1.2022.2/plantuml-1.2022.2.jar"


if [ ! -f $SCRIPT_DIR/plantuml.jar ]; then
    curl -L $PLANTUML_JAR_LINK -o $SCRIPT_DIR/plantuml.jar
fi

#use redirection for the loop to prevent to prevent subshell from pipe
#https://stackoverflow.com/questions/9612090/how-to-loop-through-file-names-returned-by-find
modules=""
while read -d $'\0' file
do
  java -jar $SCRIPT_DIR/plantuml.jar $file -tsvg
done < <(find $SCRIPT_DIR -type f -name "*.puml" -print0)
