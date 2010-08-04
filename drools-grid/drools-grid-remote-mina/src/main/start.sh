#!/bin/sh

#  build the classpath
SERVER_CLASSPATH=
for i in `ls ./libs/*.jar`
do
  SERVER_CLASSPATH=${SERVER_CLASSPATH}:${i}
done

# execute the server
java -cp ".:${SERVER_CLASSPATH}" org.drools.grid.remote.mina.MinaNodeRunner --address 127.0.0.1 --port 9123