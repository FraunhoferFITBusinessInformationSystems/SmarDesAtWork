@echo off

cd /d %~dp0%
StartJava start DatabaseManager "HSQLDB Database Manager" org.hsqldb.util.DatabaseManager
