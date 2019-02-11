@echo off

SETLOCAL

SET JAVA_HOME=${jre8.home}

REM
REM Setup class path
REM
set CP=${dependency.cp.windows}
set CP=%CP%;lib\${project.build.finalName}.${project.packaging}

REM
REM Main class and VM arguments
REM
SET RUNCLASS=com.camline.projects.smardes.clients.ToDoClients
SET JAVAOPTS=-Dlog4j.configuration=file:properties/log4j.properties
SET DBGOPTS=
REM SET DBGOPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y

REM
REM Finally - let's start
REM
cd /d %~dp0%\..
"%JAVA_HOME%\bin\java.exe" -classpath %CP% %JAVAOPTS% %DBGOPTS% %RUNCLASS% %1 %2 %3 %4 %5 %6
