:: 
:: Copyright (c) Vogler Engineering GmbH. All rights reserved.
:: Licensed under the MIT License. See LICENSE.md in the project root for license information.
::

:: @ECHO off

SET serviceName=SmartDevicesGateway

sc stop %serviceName%
ping 127.0.0.1 -n 2 > nul

sc delete %serviceName%
