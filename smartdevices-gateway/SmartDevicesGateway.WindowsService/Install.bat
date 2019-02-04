:: 
:: Copyright (c) Vogler Engineering GmbH. All rights reserved.
:: Licensed under the MIT License. See LICENSE.md in the project root for license information.
::

:: @ECHO off

SET serviceName=SmartDevicesGateway
SET binName=SmartDevicesGateway.exe
set binPath=%~dp0%binName%

sc create %serviceName% binPath=%binPath% DisplayName="SmarDe's@Work Gateway"
sc config %serviceName% start=auto
:: sc config %serviceName% obj="NT AUTHORITY\LocalService" start=auto
sc failure %serviceName%  actions= restart/180000/restart/180000/""/180000 reset= 86400 
sc description %serviceName% "Provides REST API for Smart Devices in the SmarDe's@Work solutions"
sc start %serviceName%