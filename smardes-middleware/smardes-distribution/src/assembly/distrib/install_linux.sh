#!/bin/sh

ARTEMIS_DIR=apache-artemis-2.6.4
ARTEMIS_BROKER_DIR=apache-artemis-2.6.4-broker
SMARTDES_DIR=smardes-backend-services-1.5.2


echo "**** Step 1: Creating Apache Artemis Broker Instance ****"

chmod +x apache-artemis-2.6.4/bin/artemis

./apache-artemis-2.6.4/bin/artemis create --user "smardes" --password "smardes" --http-port 80 --require-login "$ARTEMIS_BROKER_DIR"

echo "**** Step 2: Patching configuration files ****"

cp install/overwrite/bin/* $ARTEMIS_BROKER_DIR/bin
cp install/overwrite/etc/* $ARTEMIS_BROKER_DIR/etc

echo "Add more users to $ARTEMIS_BROKER_DIR/etc/artemis-users.properties"
cat install/patched/etc/artemis-users-appendix.properties >> $ARTEMIS_BROKER_DIR/etc/artemis-users.properties

echo "Add sdgw address to $ARTEMIS_BROKER_DIR/etc/broker.xml"
cp $ARTEMIS_BROKER_DIR/etc/broker.xml $ARTEMIS_BROKER_DIR/etc/broker.xml.orig
java -jar install/Saxon-HE-9.8.0-12.jar $ARTEMIS_BROKER_DIR/etc/broker.xml.orig install/patched/etc/patch-broker-xml.xslt > $ARTEMIS_BROKER_DIR/etc/broker.xml