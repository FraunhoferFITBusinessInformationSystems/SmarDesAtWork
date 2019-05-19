#!/bin/sh

ARTEMIS_DIR=apache-artemis-2.6.4
ARTEMIS_BROKER_DIR=apache-artemis-2.6.4-broker
SMARTDES_DIR=smardes-backend-services-1.5.2

# Function to prefix outputs
tag() { stdbuf -oL sed "s%^%$1: %"; }

echo "**** Step 1: Starting Artemis and Smart Devices Backend"

./$ARTEMIS_BROKER_DIR/bin/artemis run  2>&1 | tag ARTEMIS &

echo "Waiting 10 Seconds for Artemis to start..."
sleep 10

echo "**** Step 2: Starting Smart Devices Backend ****"

./$SMARTDES_DIR/bin/StartSDServices.sh 2>&1 | tag SMARDES
