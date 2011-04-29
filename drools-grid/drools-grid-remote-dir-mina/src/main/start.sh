#!/bin/sh

#  build the classpath
SERVER_CLASSPATH=
for i in `ls ./lib/*.jar`
do
  SERVER_CLASSPATH=${SERVER_CLASSPATH}:${i}
done

# execute the server
java -cp "drools-grid-remote-mina-5.3.0-SNAPSHOT.jar:${SERVER_CLASSPATH}" org.drools.grid.remote.directory.MinaDirectoryRunner --address 127.0.0.1 --port 9124
