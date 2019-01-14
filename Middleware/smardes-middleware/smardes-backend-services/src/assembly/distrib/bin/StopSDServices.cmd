@echo off

cd /d %~dp0%
call StartJava run com.camline.projects.smardes.common.jms.ShutdownComponent StartServices
SET exitcode=%errorlevel%

IF "%exitcode%" == "0" goto end

echo Graceful shutdown did not work. Kill service...
wmic process where "Name like '%%java%%' AND CommandLine like '%%-Dsmardes.app.id=SDServices01%%'" delete

:end