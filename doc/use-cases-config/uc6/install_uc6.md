# Installation Notes for Use Case 6
Follow these steps to start the SmartDevices-Gatweway for the Use Case 5 Sample:

# 1. General

 * Setup the services as described in [Installation](../../installation.md)

# 2. smardes-middleware

## 2.1 Install UC5 configuration

UC6 makes use of the database schema, the db monitoring and the queries from UC5.
Therefore perform all installation steps described in UC5 for the `smardes-middleware` component.

## 2.2 Disable UC5 Rules

To disable the UC5 live data rules delete rules/uc5.xml or move it to another place.

## 2.3 Install UC6 configuration

* Copy the whole content of `uc6/smardes-middleware/` into `smardes-middleware-x.y.z\smardes-backend-services-x.y.z`

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
            user1
            user2
         ```
    * `Device Id` - A valid Device-Id
      * Convention: For all phones and tablets "A", for all watches "B".
