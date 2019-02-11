# Installation Instructions

To ease the installation there is a combined shipment of

* Java Runtime Environment
* Apache ActiveMQ Artemis Distribution
* SmartDes Backend Services

in one archive. The SmarDes Backend Services are preconfigured for the Java Runtime Environment and Apache ActiveMQ
Artemis contained in this delivery-

## Installation Steps

* Unpack smardes-middleware-x.y.z.zip in the final installation folder
* Enter smardes-middleware-x.y.z directory
* Run install.bat
  * It opens a second window with the artemis broker creation. By default, the following is configured
    * Creates the broker directory as sibling
    * Artemis User: smardes
    * Artemis Password smardes
    * Allow anonymous login: no
    * Additionally for the SmartDevice Gateway component
      * Additional users
      * Fixed multicast address sdgw
  * After the broker instance is created the broker instance gets modified, e.g.
    * logging.properties is replaced
    * Windows service related files are copied into bin directory
    * artemis-rules.properties, artemis-roles-properties and broker.xml are patched for SmartDevice Gateway
  * It opens an admin command window in order to install the services at Windows. 
    In case of an upgrade it might be needed to stop and uninstall them before. More details are in the following subsections.

## Installation Notes for Apache ActiveMQ Artemis

The automated installation creates a default configuration which might not match with the installation environment.
To modify the configuration please refer to the
[installation instructions](https://activemq.apache.org/artemis/docs/latest/using-server.html)
on the Apache ActiveMQ Artemis web page.

## Notes on installing Artemis and SmarDes Rule Engine as Windows services

To be able to install the windows services a command prompt with administrator rights is needed.

To install Artemis as a windows service go to apache-artemis-a.b.c-broker\bin and execute

    ..\apache-artemis-a.b.c-broker\bin>service install

Similarly, go to smardes-backend-services-x.y.z\bin and execute

    ..\smardes-backend-services-x.y.z\bin>service install

It might be needed to stop and uninstall an old version of the services first.
