#!/bin/sh

if [ -z "$IRUTILS_HOME" ]
then
   IRUTILS_HOME=.
fi

export LD_LIBRARY_PATH=/usr/local/lib
export _SILENT_JAVA_OPTIONS=-Xmx4g
unset _JAVA_OPTIONS
#java -Xmx16g -Djava.library.path=~/lib/ -cp "lib/*" $@
java  -cp "target/biocaddie-0.0.1-SNAPSHOT.jar:target/lib/*:lib/*" $@
