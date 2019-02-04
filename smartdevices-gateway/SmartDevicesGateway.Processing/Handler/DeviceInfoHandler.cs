//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using Microsoft.Extensions.Logging;

using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Processing.Exceptions;
using SmartDevicesGateway.Services;
using SmartDevicesGateway.Services.ConfigService;
using SmartDevicesGateway.Services.FcmService;
using SmartDevicesGateway.Services.FcmService.Requests;

namespace SmartDevicesGateway.Processing.Handler
{
    public class DeviceInfoHandler
    {
        private readonly ILoggerFactory _loggerFactory;
        private readonly IPersistenceProvider _persistenceProvider;
        private readonly IConfigService _configService;

        public DeviceInfoHandler(ILoggerFactory loggerFactory, IPersistenceProvider persistenceProvider, IConfigService configService)
        {
            _loggerFactory = loggerFactory;
            _persistenceProvider = persistenceProvider;
            _configService = configService;
        }

        public DeviceInfo GetDeviceInfo(DeviceId deviceId)
        {
            return _persistenceProvider.TryGetDeviceInfos(deviceId, out var info) ? info : null;
        }

        public void UpdateDeviceInfo(DeviceId deviceId, DeviceInfo deviceInfo)
        {
            _persistenceProvider.AddOrUpdateDeviceInfos(deviceId, deviceInfo);
        }
    }
}
