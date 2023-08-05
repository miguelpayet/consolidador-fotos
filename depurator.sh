#!/bin/bash

if [ -z "$1" ]; then
  exit 1
fi

dir_name=$1

if [ "$dir_name" = "." ]; then
    exit 1
fi

if echo $dir_name | grep -q ' '; then
    exit 1
fi

# Get the list of all files in the directory
files=$(ls -1 $dir_name)

# Check if the directory is empty
if [ -z "$files" ]; then
  echo "The directory is empty."
  exit 0
fi

new_dir_name="${dir_name%?}"

# Rename the directory
mv $dir_name $new_dir_name
destination_dir=$(dirname "$new_dir_name")

# Find the size of the largest file
largest_size=0
for file in $new_dir_name/*; do
  if [ -f "$file" ]; then
    size=$(stat -f %z "$file")
    if (( size > largest_size )); then
      largest_size=$size
    fi
  fi
done

# Loop through the list of files and delete all files except for the largest one
for file in $new_dir_name/*; do
  file_size=$(stat -f %z "$file")
  if [[ $file_size -lt $largest_size ]]; then
    rm "$file"
  else
    mv "$file" $destination_dir
  fi
done

rmdir $new_dir_name