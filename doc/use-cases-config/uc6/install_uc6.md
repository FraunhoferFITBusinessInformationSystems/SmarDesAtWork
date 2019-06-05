# Installation Notes for Use Case 5
Follow these steps to start the SmartDevices-Gatweway for the Use Case 5 Sample:

__Note: This page is not finished yet__

# 1. General

 * Setup the services as described in [Installation](../../installation.md)

# 2. smardes-middleware

# 3. smartdevices-gateway

  * Copy the whole content of `uc6/smartdevices-gateway/config/` into `smartdevices-gateway/config/`
  * Start the smartdevices-gateway
    * Check if smartdevices-gateway is accessable via swagger:
         > http://localhost:7000/swagger/index.html

# 4. smartdevices-app

  * Configure the Settings in the Client-Application:
    * `Server Address` - Url to the Server installation like "http://192.168.0.1"
    * `Server Port` - Port of the Server
    * `Username` - A valid Username
      * Pick a Username from the Configuration File `smartdevices-gateway/config/UserConfig.json`
      * In this Example one of
         ```
            operator1
            operator2
            operator3
            operator4
         ```
    * `Device Id` - A valid Device-Id
      * Convention: For all phones and tablets "A", for all watches "B".
