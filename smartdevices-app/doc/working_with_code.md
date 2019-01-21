# Prerequisites
* Android Studio 3.2.1 or newer [Download](https://developer.android.com/studio/#downloads)
    * Android SDK (Installed with Android Studio)
    * Android SDK Platform Package for API-Level 23 and 26 (Download in Android Studio "Preferences > Appearance & Behavior > System Settings > Android SDK")
    * Android SDK Tools (Download in Android Studio "Preferences > Appearance & Behavior > System Settings > SDK Tools")
        * Android SDK Build-Tools
        * Android SDK Platform-Tools
        * Android SDK Tools
        * Google Play services
* Android Studio Plugins are required:
    * IntelliJ Lombok plugin

# Build the projects
* **Build with Android Studio.** Open the repository folder as Project. This Project contains all Android Modules and all Library Modules. Build the "smartdevices.app.phone" Module for the Phone and Tablet app and the "smartdevices.app.watch" Module for the Watch app.
* **Build from command prompt.** Run the build/build.cmd script to build the solution.

# Run the tests
* Most of the tests require the [smartdevices-gateway](http://github.com/blub) to run. Update the address (hostname, port, etc) in the integrationtest.properties file to match the smartdevices-gateway before you run the tests.
--TODO

# Start coding
* First take a look at the [documentation](http://github.com/). 
* The API documentation today is in the source code. We will create a wiki page for that soon.

--TODO
