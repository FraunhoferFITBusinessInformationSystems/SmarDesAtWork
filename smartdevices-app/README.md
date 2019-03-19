# SmarDe's@Work: smartdevices-app

The Android app is mostly a stateless visualization of Data provided by the [smartdevice-gateway](../smartdevices-gateway/README.md). The SDGW dictates the SmartDevices what it should do and what it should display.

This is achieved by generating the UI-Elements at Runtime based on the configuration data provided by the SDGW. See [Interface Definition](doc/interface.md) for a detailed look at the used Interface.

## App-Documentation
See the [App Documentation](doc/documentation-app.md) for more detailed Infomation about the App.

# Getting Started

See [Working with Code](doc/working_with_code.md) Page for more information.

## Requirements

* It can be assumed, that a connection to the Server is always present. (Either over wlan or mobile internet) If not, the app can not be used.
* App has no Caching capabilities. All caching is done in the SDGW.
* SDGW and the SmartDevices-Middleware-Services are running in the Network

# Terms
* **DI** - Dependency Injection: Software-Pattern to distribute and automate Class-Generation and 
* **SDGW** - Abbreviation for Smartdevices-Gateway
* **SD** - SmartDevice
* **DeviceId** - An Identification for a specific device. The DeviceId has a general format of "{name}.{id}" (RegEx: [a-zA-Z0-9]\*\\.[a-zA-Z0-9]\*)

# Used Libraries

* Dagger (DI)
* Lombok (Code Generation)
* RxJava (Async calls)
* ... (TODO)



