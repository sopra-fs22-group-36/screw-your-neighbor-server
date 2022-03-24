#!/usr/bin/bash
set -e
set -x

SCRIPT_DIR=$(dirname $(realpath $0))
LATEX_HEADER_FILE=$SCRIPT_DIR/doc/latex/options.tex

#use redirection for the loop to prevent to prevent subshell from pipe
#https://stackoverflow.com/questions/9612090/how-to-loop-through-file-names-returned-by-find
modules=""
while read -d $'\0' file
do
  name_without_ending=$(echo $file | sed "s|.md||")
  tmp_file_name=${name_without_ending}_tmp.md
  sed 's/<br>/\\linebreak/g' $file > $tmp_file_name
  output_path=$(echo $file | sed "s|.md|.pdf|")
  pandoc --include-in-header=$LATEX_HEADER_FILE --resource-path=$(dirname $file) $tmp_file_name -o $output_path
  rm $tmp_file_name
done < <(find $SCRIPT_DIR -type f -name "*.md" ! -name README.md -print0)
