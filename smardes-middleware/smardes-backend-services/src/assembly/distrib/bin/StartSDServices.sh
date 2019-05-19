#!/bin/sh

#
# check java home environment variable
#
#if [ ! -x "$JAVA_HOME/bin/java" ]: then
#	echo "The JAVA_HOME environment variable is not defined correctly"
#	echo "Set the JAVA_HOME environment variable to point to the directory"
#	echo "where the Java runtime environment (JRE) is installed."
#	exit 1
#fi

# Go to directory where start script is located
cd `dirname $0`/..

SERVICE_NAME=SDServices01
SMARDES_VERSION=1.5.2

#
# Setup class path
#
CP=lib/jandex-2.0.5.Final.jar:lib/poi-4.0.1.jar:lib/poi-ooxml-schemas-4.0.1.jar:lib/netty-transport-native-kqueue-4.1.32.Final-osx-x86_64.jar:lib/log4j-1.2.17.jar:lib/smardes-common-jpa-1.5.2.jar:lib/johnzon-mapper-1.1.11.jar:lib/jaxb-api-2.3.1.jar:lib/txw2-2.3.1.jar:lib/FastInfoset-1.2.15.jar:lib/c3p0-0.9.5.2.jar:lib/javax.json.bind-api-1.0.jar:lib/log4jdbc-1.2.jar:lib/jboss-transaction-api_1.2_spec-1.1.1.Final.jar:lib/juel-impl-2.2.7.jar:lib/smardes-rule-engine-api-1.5.2.jar:lib/antlr-2.7.7.jar:lib/commons-io-2.6.jar:lib/netty-codec-4.1.32.Final.jar:lib/hibernate-core-5.4.1.Final.jar:lib/classmate-1.3.4.jar:lib/commons-math3-3.6.1.jar:lib/smardes-todo-api-1.5.2.jar:lib/hibernate-commons-annotations-5.1.0.Final.jar:lib/juel-api-2.2.7.jar:lib/netty-buffer-4.1.32.Final.jar:lib/jboss-logging-3.3.2.Final.jar:lib/smardes-resources-1.5.2.jar:lib/javax.activation-api-1.2.0.jar:lib/hsqldb-2.4.1.jar:lib/netty-codec-http-4.1.32.Final.jar:lib/smardes-resources-api-1.5.2.jar:lib/stax-ex-1.8.jar:lib/commons-codec-1.11.jar:lib/johnzon-core-1.1.11.jar:lib/commons-collections4-4.2.jar:lib/commons-csv-1.6.jar:lib/netty-handler-4.1.32.Final.jar:lib/proton-j-0.31.0.jar:lib/netty-transport-native-epoll-4.1.32.Final-linux-x86_64.jar:lib/velocity-engine-core-2.0.jar:lib/istack-commons-runtime-3.0.7.jar:lib/smardes-json-api-1.5.2.jar:lib/smardes-maindata-1.5.2.jar:lib/smardes-todo-1.5.2.jar:lib/slf4j-log4j12-1.7.25.jar:lib/smardes-maindata-api-1.5.2.jar:lib/commons-compress-1.18.jar:lib/geronimo-json_1.1_spec-1.1.jar:lib/xmlbeans-3.0.2.jar:lib/johnzon-jsonb-1.1.11.jar:lib/mchange-commons-java-0.2.11.jar:lib/poi-ooxml-4.0.1.jar:lib/byte-buddy-1.9.5.jar:lib/hibernate-c3p0-5.4.1.Final.jar:lib/smardes-rule-engine-1.5.2.jar:lib/jaxb-runtime-2.3.1.jar:lib/netty-common-4.1.32.Final.jar:lib/smardes-common-1.5.2.jar:lib/commons-lang3-3.8.1.jar:lib/geronimo-jms_2.0_spec-1.0-alpha-2.jar:lib/netty-transport-4.1.32.Final.jar:lib/slf4j-api-1.7.25.jar:lib/netty-transport-native-unix-common-4.1.32.Final.jar:lib/qpid-jms-client-0.40.0.jar:lib/netty-resolver-4.1.32.Final.jar:lib/smardes-dbaccess-1.5.2.jar:lib/javax.persistence-api-2.2.jar:lib/javassist-3.24.0-GA.jar:lib/curvesapi-1.05.jar:lib/dom4j-2.1.1.jar
CP=$CP:lib/smardes-backend-services-${SMARDES_VERSION}.jar

# Extra class path for external database - please check legal concerns
CP=$CP:externaldb:externaldb/lib/sqljdbc42-4.2.6420.100.jar

#
# run class and java options
#

RUNCLASS=com.camline.projects.smardes.StartServices
JAVAOPTS="-Dlog4j.configuration=file:properties/log4j.properties -Dlog4jdbc.dump.sql.maxlinelength=0 -Dsmardes.app.id=$SERVICE_NAME"

if [ -n "$ARTEMIS_URL" ]; then
   sed -i "s/\(address=\).*\$/\1${ARTEMIS_URL}/" properties/artemis.properties
   echo 111
fi

$JAVA_HOME/bin/java -classpath $CP $JAVAOPTS $RUNCLASS $*

