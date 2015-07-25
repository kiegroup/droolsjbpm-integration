#!/bin/sh

# Change directory to the directory of the script
cd `dirname $0`

mainClasspath=
for i in binaries/*.jar; do mainClasspath=${mainClasspath}:$i; done
for i in ../binaries/*.jar; do mainClasspath=${mainClasspath}:$i; done
mainClass=org.drools.examples.DroolsJbpmIntegrationExamplesApp

echo "Usage: runExamples.bat"
echo "Notes:"
echo "- Java must be installed. Get the JRE ^(http://www.java.com^) or the JDK."
echo "- For optimal performance, Java is recommended to be OpenJDK 7 or higher."
echo "- For JDK, the environment variable JAVA_HOME should be set to the JDK installation directory"
echo "  For example: set "JAVA_HOME=C:\Program Files\Java\jdk1.6.0""
echo "- The working dir should be the directory of this script."
echo "Starting examples app..."

# You can use -Xmx128m or less too, but it might be slower
if [ -f $JAVA_HOME/bin/java ]; then
    $JAVA_HOME/bin/java -Xms256m -Xmx512m -server -cp ${mainClasspath} ${mainClass} $*
else
    java -Xms256m -Xmx512m -cp ${mainClasspath} ${mainClass} $*
fi

if [ $? != 0 ] ; then
    echo
    echo "Error occurred. Check if \$JAVA_HOME ($JAVA_HOME) is correct."
    # Prevent the terminal window to disappear before the user has seen the error message
    sleep 20
fi
