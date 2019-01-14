REM @echo off

cd /d %~dp0%
SET JAVA_HOME=..\..\${jre.basedir}
artemis.cmd stop
