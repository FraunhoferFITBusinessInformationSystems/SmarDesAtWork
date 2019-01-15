# Building SmarDes Middleware

The SmarDes Middleware Sources are located in a Maven multi-module project.

## Prerequisites for the compilation

* OpenJDK 8
* Maven 3.5+
* Internet Connection (to be able to download dependencies during the build process)

## Building

* Enter smardes-middleware folder
* Execute the Maven build using

		$ mvn clean package

After a successful build process the resulting installation archive can be found in

		smardes-middleware\smardes-distribution\target\smardes-middleware-<version>.zip
