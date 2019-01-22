#!/bin/bash

cd "$(dirname "$0")"
cd ..

#cloc --exclude-list-file=.clocignore .

cloc --exclude-dir=$(tr '\n' ',' < .clocignore) .
