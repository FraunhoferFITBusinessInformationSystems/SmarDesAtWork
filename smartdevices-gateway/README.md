# SmarDe's@Work: smartdevices-gateway

The SmartDevices-Gateway (SDGW) works as an 'Translator' between the Rule-Engine or other Middleware-Services and the SmartDevices. It Translates Middleware-Messages from AMQP to something the smart-devices can understand and work with.

In Addition it persists status-informations about registered smartphones and notifies them via [Firebase-Cloud-Messaging](https://firebase.google.com/docs/cloud-messaging) to inform them about new and changed data.

## App-Documentation
See the [App Documentation](doc/documentation-gateway.md) for more detailed Infomation about the SDGW.

# Getting Started

## Installation
See [Installation SDGW](doc/install.md)

## Start Coding
See [Working with Code](doc/working_with_code.md) Page for more information.

## Requirements


# Terms
* **DI** - Dependency Injection: Software-Pattern to distribute and automate Class-Generation and 
* **SDGW** - Abbreviation for Smartdevices-Gateway
* **SD** - SmartDevice
* **DeviceId** - An Identification for a specific device. The DeviceId has a general format of "{name}.{id}" (RegEx: [a-zA-Z0-9]\*\\.[a-zA-Z0-9]\*)

# Used Libraries
