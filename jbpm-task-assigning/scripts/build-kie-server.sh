# Development script for quickly generating a kie-server.war file that contains all the task assigning
# related jars until this code is moved to the final location in kie-sever-parent project structure and is
# automatically packaged by the kie-server assemblies.

# To make the packaging/script work do this:

# Configure the following variables with your development directories.

# 0) snapshot we are working with
VERSION=7.35.0-SNAPSHOT

# 1) local task assigning project dir.
PROJECT_DIR=/home/wmedvede/development/projects/droolsjbpm/droolsjbpm-integration/jbpm-task-assigning

# 2) local kie-server-parent project dir.
KIE_SERVER_PROJECT=/home/wmedvede/development/projects/droolsjbpm/droolsjbpm-integration/kie-server-parent

# 3) path to the kie-server distribution war file you want to use.

KIE_SERVER_DIST=/home/wmedvede/development/projects/droolsjbpm/droolsjbpm-integration/kie-server-parent/kie-server-wars/kie-server/target/kie-server-$VERSION-ee8.war

# 4) build the kie-server-parent project so that the distribution file is generated and incorporates the task assigning changes on that project.

# 5) finally execute this build-kie-server.sh script in the jbpm-task-assigning/scripts directory


# NO more configurations are required, below starts the kie-server.war file generation.
# When the script finishes the output directory will have the generated kie-server.war.
# drop this file in your WF installation and you'll have the task assigning working.

mkdir -p tmp
mkdir -p output

cd tmp
rm -Rf *

jar xf $KIE_SERVER_DIST

# kie-server-services-task-assigning-user-system-api-$VERSION.jar
cp $PROJECT_DIR/kie-server-services-task-assigning/kie-server-services-task-assigning-user-system/kie-server-services-task-assigning-user-system-api/target/kie-server-services-task-assigning-user-system-api-$VERSION.jar WEB-INF/lib

# kie-server-task-assigning-user-system-simple-$VERSION.jar
cp $PROJECT_DIR/kie-server-services-task-assigning/kie-server-services-task-assigning-user-system/kie-server-services-task-assigning-user-system-simple/target/kie-server-services-task-assigning-user-system-simple-$VERSION.jar WEB-INF/lib

# kie-server-services-task-assigning-core-$VERSION.jar
cp $PROJECT_DIR/kie-server-services-task-assigning/kie-server-services-task-assigning-core/target/kie-server-services-task-assigning-core-$VERSION.jar WEB-INF/lib

# kie-server-api-task-assigning-$VERSION.jar
cp $PROJECT_DIR/kie-server-api-task-assigning/target/kie-server-api-task-assigning-$VERSION.jar WEB-INF/lib

# kie-server-services-task-assigning-runtime-$VERSION.jar
cp $PROJECT_DIR/kie-server-services-task-assigning/kie-server-services-task-assigning-runtime/target/kie-server-services-task-assigning-runtime-$VERSION.jar WEB-INF/lib

# kie-server-rest-task-assigning-runtime-$VERSION.jar
cp $PROJECT_DIR/kie-server-rest-task-assigning-runtime/target/kie-server-rest-task-assigning-runtime-$VERSION.jar WEB-INF/lib

# kie-server-services-task-assigning-planning-$VERSION.jar
cp $PROJECT_DIR/kie-server-services-task-assigning/kie-server-services-task-assigning-planning/target/kie-server-services-task-assigning-planning-$VERSION.jar WEB-INF/lib

# kie-server-client-task-assigning-$VERSION.jar
cp $PROJECT_DIR/kie-server-client-task-assigning/target/kie-server-client-task-assigning-$VERSION.jar WEB-INF/lib

# Speed up for development, avoid re-generating the kie-server assembly if I only compile a change related with task assigning.
rm WEB-INF/lib/kie-server-services-jbpm*.jar 
rm WEB-INF/lib/kie-server-api-7*.jar

cp $KIE_SERVER_PROJECT/kie-server-services/kie-server-services-jbpm/target/kie-server-services-jbpm-$VERSION.jar WEB-INF/lib

cp $KIE_SERVER_PROJECT/kie-server-api/target/kie-server-api-$VERSION.jar WEB-INF/lib

jar -cf kie-server.war *

cd ..

mv tmp/kie-server.war output
