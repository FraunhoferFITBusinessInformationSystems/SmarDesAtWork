@echo off
cd %~dp0\..

REM Set a short directory path for the gradle build cache because some generated files have too
REM long paths for windows.
gradlew.bat test -g C:\tmp\gradle-cache