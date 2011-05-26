#!/bin/sh

#  build the classpath
SERVER_CLASSPATH=
for i in `ls ./lib/*.jar`
do
  SERVER_CLASSPATH=${SERVER_CLASSPATH}:${i}
done

# execute the server
java -cp "drools-grid-remote-dir-mina-5.2.0.CR1.jar:${SERVER_CLASSPATH}" org.drools.grid.remote.mina.MinaNodeRunner --address 127.0.0.1 --port 9123
