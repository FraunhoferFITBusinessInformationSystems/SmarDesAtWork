@echo off

cd /d %~dp0%
for /f %%i in ('cd') DO set home=%%i
set JAVA_HOME=%home%\${jre.basedir}

SET ARTEMIS_DIR=apache-artemis-${artemis.version}
SET ARTEMIS_BROKER_DIR=apache-artemis-${artemis.version}-broker

echo.
echo **** Step 1: Creating Apache Artemis Broker Instance ****
echo.
echo.

call %ARTEMIS_DIR%\bin\artemis.cmd create --user smardes --password smardes --require-login %ARTEMIS_BROKER_DIR%

echo.
echo.
echo.
echo **** Step 2: Patching configuration files ****
echo.
copy install\overwrite\bin\* %ARTEMIS_BROKER_DIR%\bin
copy install\overwrite\etc\* %ARTEMIS_BROKER_DIR%\etc

echo Add more users to %ARTEMIS_BROKER_DIR%\etc\artemis-users.properties
type install\patched\etc\artemis-users-appendix.properties >> %ARTEMIS_BROKER_DIR%\etc\artemis-users.properties

echo Add "sdgw" address to %ARTEMIS_BROKER_DIR%\etc\broker.xml
copy %ARTEMIS_BROKER_DIR%\etc\broker.xml %ARTEMIS_BROKER_DIR%\etc\broker.xml.orig
%JAVA_HOME%\bin\java -jar install\Saxon-HE-9.8.0-12.jar %ARTEMIS_BROKER_DIR%\etc\broker.xml.orig install\patched\etc\patch-broker-xml.xslt > %ARTEMIS_BROKER_DIR%\etc\broker.xml

echo.
echo.
echo.
echo **** Step 4: Register services ****
echo.
echo Now use the Admin Command Prompt that was just opened
echo from this batch file and install the services
echo in %ARTEMIS_BROKER_DIR%\bin using
echo    service install
echo in smardes-backend-services-${project.version}\bin using
echo    service install
echo.
echo Now opening admin command window for registering the services.
echo When finished exit admin command window to continue.

powershell -Command "Start-Process cmd -ArgumentList '/K cd %home%\%ARTEMIS_BROKER_DIR%\bin' -Verb RunAs "

pause