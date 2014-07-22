#!/bin/sh
VERSION=`basename assembly/target/codenvy-cli-*/`

ASSEMBLY_BIN_DIR=assembly/target/$VERSION/$VERSION/bin

if [ ! -d "${ASSEMBLY_BIN_DIR}" ]
then
  echo "$(tput setaf 1)The command 'mvn clean install' needs to be run first."$(tput sgr0)
  exit 1
fi

cd $ASSEMBLY_BIN_DIR
./codenvy $*
