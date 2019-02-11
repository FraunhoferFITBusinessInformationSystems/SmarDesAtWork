@echo off
setlocal EnableExtensions EnableDelayedExpansion


rem Enter directory of service.bat
cd /d %~dp0%
for /f %%i in ('cd') DO set INSTALL_HOME=%%i

rem Service Settings
rem SHORTNAME	Short name to identify a service for install and uninstall; used for dependencies
rem DISPLAYNAME	Name as shown in Windows Services application
rem DESCRIPTION	Further description shown in Services' property dialog
rem STARTSCRIPT	Start script
rem STOPSCRIPT	Stop script
rem -------------------------------------------------------------------------------------------------
set SHORTNAME=ActiveMQArtemis
set DISPLAYNAME="SmarDe's@Work ActiveMQ Artemis"
set DESCRIPTION="ActiveMQ Artemis MQ broker for SmarDe's@Work solutions"
set STARTSCRIPT=StartArtemis.cmd
set STOPSCRIPT=StopArtemis.cmd

rem Dependent Services
rem The short name of dependent services separated with '#' or ';'
rem -------------------------------------------------------------------------------------------------
set DEPENDENCIES=

rem The log level for service start and the log directory
rem -------------------------------------------------------------------------------------------------
set LOGLEVEL=INFO
set LOGPATH=..\log

rem The prunsrv executable
rem -------------------------------------------------------------------------------------------------
set PRUNSRV=..\..\commons-daemon-1.1.0\amd64\prunsrv.exe

if /I "%1" == "install"   goto cmdInstall
if /I "%1" == "uninstall" goto cmdUninstall
if /I "%1" == "start"     goto cmdStart
if /I "%1" == "stop"      goto cmdStop
if /I "%1" == "restart"   goto cmdRestart


:cmdUsage
echo Usage:
echo   service install [/loglevel INFO]
echo   service uninstall
echo   service start
echo   service stop
echo   service restart
echo(
echo The options for "service install":
echo   /loglevel          : the log level for the service:  Error, Info, Warn or Debug ^(Case insensitive^), default: INFO
echo(
goto endBatch


:cmdInstall
shift

:LoopArgs
if "%1"=="" goto doInstall

if /I "%1"=="/loglevel" (
  if /I not "%2"=="Error" if /I not "%2"=="Info" if /I not "%2"=="Warn" if /I not "%2"=="Debug" (
    echo ERROR: /loglevel must be set to Error, Info, Warn or Debug ^(Case insensitive^)
    goto endBatch
  )
  set LOGLEVEL=%2
  shift
  shift
  goto LoopArgs
)
echo ERROR: Unrecognized option: %1
echo(
goto cmdUsage


:doInstall
  set STARTPARAM="/c \"set NOPAUSE=Y ^^^&^^^& %STARTSCRIPT%\""
  set STOPPARAM="/c \"set NOPAUSE=Y ^^^&^^^& %STOPSCRIPT%\""

echo Installing %DISPLAYNAME% ^(%SHORTNAME%^)...

%PRUNSRV% install %SHORTNAME% --Startup=auto ++DependsOn=%DEPENDENCIES%^
	--DisplayName=%DISPLAYNAME% --Description %DESCRIPTION%^
	--LogLevel=%LOGLEVEL% --LogPath="%LOGPATH%" --LogPrefix=service --StdOutput=auto --StdError=auto^
	--StartMode=exe --StartImage=cmd.exe --StartPath="%INSTALL_HOME%" ++StartParams=%STARTPARAM%^
	--StopMode=exe --StopImage=cmd.exe --StopPath="%INSTALL_HOME%"  ++StopParams=%STOPPARAM%
goto cmdEnd


:cmdUninstall
echo Uninstalling %DISPLAYNAME% ^(%SHORTNAME%^)...
%PRUNSRV% stop %SHORTNAME%
if "%errorlevel%" == "0" (
  %PRUNSRV% delete %SHORTNAME%
) else (
  echo Unable to stop the service
)
goto cmdEnd


:cmdStart
echo Starting %DISPLAYNAME% ^(%SHORTNAME%^)...
%PRUNSRV% start %SHORTNAME%
goto cmdEnd


:cmdStop
echo Stopping %DISPLAYNAME% ^(%SHORTNAME%^)...
%PRUNSRV% stop %SHORTNAME%
goto cmdEnd


:cmdRestart
echo Restarting %DISPLAYNAME% ^(%SHORTNAME%^)...
%PRUNSRV% stop %SHORTNAME%
if "%errorlevel%" == "0" (
  %PRUNSRV% start %SHORTNAME%
) else (
  echo Unable to stop the service
)
goto cmdEnd


:cmdEnd
if errorlevel 8 (
  echo ERROR: The service %SHORTNAME% already exists
  goto endBatch
)
if errorlevel 2 (
  echo ERROR: Failed to load service configuration
  goto endBatch
)
if errorlevel 0 (
  echo Success
  goto endBatch
)
echo errorlevel=%errorlevel%


:endBatch
@echo on
