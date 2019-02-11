REM @echo off

cd /d %~dp0%
StartJava start SDServices01 "SmarDe's Services" com.camline.projects.smardes.StartServices
pause