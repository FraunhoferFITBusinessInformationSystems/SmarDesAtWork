@echo off

REM This is a generic start script and not intended to be executed directly

if "%1" == "run" goto goon
if "%1" == "start" goto goon

echo StartJava is not intended to be executed directly
pause
exit

:goon
SETLOCAL

cd /d %~dp0%\..
SET JAVA_HOME=..\${jre.basedir}

REM
REM Setup class path
REM
set CP=${dependency.cp.windows}
set CP=%CP%;lib\${project.build.finalName}.${project.packaging}
REM
REM Extra class path for external database - please check legal concerns
set CP=%CP%;externaldb;externaldb\lib\sqljdbc42.jar

REM
REM Standard VM arguments
REM
SET JAVAOPTS=-Dlog4j.configuration=file:properties/log4j.properties -Dlog4jdbc.dump.sql.maxlinelength=0 -Dsmardes.app.id=%2

REM
REM Finally - let's start
REM
if "%1" == "run" (
	"%JAVA_HOME%\bin\java.exe" -classpath %CP% %JAVAOPTS% %2 %3 %4
)
if "%1" == "start" (
	start %3 "%JAVA_HOME%\bin\java.exe" -classpath %CP% %JAVAOPTS% %4 %5 %6
)

