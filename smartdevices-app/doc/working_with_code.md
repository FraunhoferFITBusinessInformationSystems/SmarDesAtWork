# Prerequisites
* Android Studio 3.2.1 or newer [Download](https://developer.android.com/studio/#downloads)
    * Android SDK (Installed with Android Studio)
    * Android SDK Platform Package for API-Level 23 and 26 (Download in Android Studio "Preferences > Appearance & Behavior > System Settings > Android SDK")
    * Android SDK Tools (Download in Android Studio "Preferences > Appearance & Behavior > System Settings > SDK Tools")
        * Android SDK Build-Tools
        * Android SDK Platform-Tools
        * Android SDK Tools
        * Google Play services
* If you want to use the Command line tools with Windows, you need to set some Environment-Variables
  * 'ANDROID_HOME' needs to be set to your AndroidSDK installation directory
  * Add the following Path to the Path-Variable: %ANDROID_HOME%\platform-tools\
* Android Studio Plugins are required:
    * IntelliJ Lombok plugin

# Build the projects
* **Build with Android Studio.** 
   1. Open the repository folder as Project. This Multi-Module-Project contains all Android Modules and all Library Modules.
   2. Sync Gradle on the Project. If you Build/Sync the Project for the first time, there will be Build errors. This will resolve after the first Build.
   3. Build the "smartdevices.app.phone" Module for the Phone and Tablet app and the "smartdevices.app.watch" Module for the Watch app.
* **Build from command prompt.** Run the build/build.cmd (Win) or build/build.sh (Unix) script to build the solution.

# Run the tests
* Most of the tests require the [smartdevices-gateway](../../smartdevices-gateway/README.md) to run. Update the address (hostname, port, etc) in the integrationtest.properties file to match the smartdevices-gateway before you run the tests.
--TODO

# Start coding
* First take a look at the [documentation](documentation-app.md). 
* The API documentation today is in the source code. We will create a wiki page for that soon.

--TODO
