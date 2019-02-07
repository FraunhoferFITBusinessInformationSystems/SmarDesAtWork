@echo off

cd /d %~dp0%
SET JAVA_HOME=..\..\${jre.basedir}

:: sqlline.bat - Windows script to launch SQL shell
"%JAVA_HOME%\bin\java.exe" -cp "%~dp0\lib\*" sqlline.SqlLine %*

:: End sqlline.bat