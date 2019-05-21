# Build & Install of smartdevices-gateway

## Prerequisites
  * Latest Version of [dotnet-core 2.2 (For Visual Studio 2017)](https://dotnet.microsoft.com/download/dotnet-core/2.2)
  * Visual Studio 2017 (not neccessary)

## Debug with Visual Studio
  * Open `smartdevices-gateway/SmartDevicesGateway.sln` with Visual Studio 2017 or later.
  * Adjust the configuration file in `SmartDevicesGateway.Api/config/ApplicationSettings.json` to match your Environment:
    * `AmqpServiceConfig:ConnectionString` - Connection String used to connect to the AMQP-Broker
    * `FcmServiceConfig:ServerKey` - The Firebase Server Token to allow the SDGW to send Messages to Firebase
  * Select Project `SmartDevicesGateway.WindowsService` as Startup-Project
  * Start Debugging


## Build with Command Line
### Windows
  * Open CMD in `/smartdevices-gateway/`
  * Run:
```
$> dotnet publish
```
  * All generated files are located in     
```
SmartDevicesGateway.WindowsService\bin\Debug\net461\win7-x64\publish
SmartDevicesGateway.NetCoreService\bin\Debug\netcoreapp2.2\publish
```
  * [Configure](#Configure) the Service
  * Start the SDGW
    * Start the Windows Runtime:                `SmartDevicesGateway.WindowsService\bin\Debug\net461\win7-x64\publish\Start.bat`
    * Install/Uninstall as Windows-Service: (Needs to run as Administrator)
    `SmartDevicesGateway.WindowsService\bin\Debug\net461\win7-x64\publish\Install.bat`
    `SmartDevicesGateway.WindowsService\bin\Debug\net461\win7-x64\publish\Uninstall.bat`
    * Start the Universal Runtine
    `SmartDevicesGateway.NetCoreService\bin\Debug\netcoreapp2.2\publish\Start.bat`

### Linux
  * cd into `/smartdevices-gateway/`
  * Run:
```
$> dotnet publish
```
  * All generated files are located in     
```
SmartDevicesGateway.NetCoreService\bin\Debug\netcoreapp2.2\publish
```
  * [Configure](#Configure) the Service
  * Start the SDGW
    * Start the Universal Runtine
    `SmartDevicesGateway.NetCoreService\bin\Debug\netcoreapp2.2\publish\Start.sh`

## Configure
  * Adjust the **general configuration** in `config/ApplicationSettings.json` to match your Environment:
    * `AmqpServiceConfig:ConnectionString` - Connection String used to connect to the AMQP-Broker
    * `FcmServiceConfig:ServerKey` - The Firebase Server Token to allow the SDGW to send Messages to Firebase
  * Use-Case-Specific configurations are
```
config/SmartDeviceConfig.json
config/UiConfig.json
config/UserConfig.json
```

## Install on other Systems
  * The Output of the `dotnet publish` can copied to any other System with [dotnet-core 2.2 runtime](https://dotnet.microsoft.com/download) installed.
  * The `SmartDevicesGateway.NetCoreService`-Project is platform-independant.
