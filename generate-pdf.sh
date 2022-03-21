#!/usr/bin/bash
set -e
set -x

SCRIPT_DIR=$(dirname $(realpath $0))

#use redirection for the loop to prevent to prevent subshell from pipe
#https://stackoverflow.com/questions/9612090/how-to-loop-through-file-names-returned-by-find
modules=""
while read -d $'\0' file
do
  output_path=$(echo $file | sed "s|.md|.pdf|")
  pandoc --resource-path=$(dirname $file) $file -o $output_path
done < <(find $SCRIPT_DIR -type f -name "*.md" ! -name README.md -print0)
