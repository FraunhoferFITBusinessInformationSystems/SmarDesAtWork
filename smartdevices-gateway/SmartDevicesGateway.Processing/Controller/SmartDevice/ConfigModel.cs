//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common;
using SmartDevicesGateway.Common.Extensions;
using SmartDevicesGateway.Model.Dto;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Enums;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Tabs;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Processing.Handler;

namespace SmartDevicesGateway.Processing.Controller.SmartDevice
{
    public class ConfigModel : AbstractModel
    {
        private readonly ConfigHandler _configHandler;
        private readonly DeviceInfoHandler _deviceInfoHandler;

        public ConfigModel(ILoggerFactory loggerFactory, ConfigHandler configHandler, DeviceInfoHandler deviceInfoHandler) : base(loggerFactory)
        {
            _configHandler = configHandler;
            _deviceInfoHandler = deviceInfoHandler;
        }

        public ConfigDto GetDeviceConfig(DeviceId deviceId)
        {
            var conf = _configHandler.GetDeviceConfig(deviceId);
            var valueDefs = _configHandler.GetValueSpecifications(conf.Values).ToList();
            var actionDefs = _configHandler.GetActions(conf.Actions);

            //Copy keys in the Component
//            valueDefs.ForEach(x => x.Component.Id = x.Name);
//            var valueSpecCopy = valueDefs.Select(x => x.Component).ToList();

            var uis = _configHandler.GetUiLayouts().ToList();

            // FIXME TODO Woraround für Newtonsoft.Json Bug
            // CamelCaseSerialization of Dictionaries

//            foreach (var def in valueDefs)
//            {
//                def.Component.AdditionalProperties = def.Component.AdditionalProperties.ConvertToLowerCamelCaseKeys();
//            }

            foreach (var ui in uis)
            {
                ui.AdditionalProperties = ui.AdditionalProperties.ConvertToLowerCamelCaseKeys();
            }

            var tabKeys = _configHandler.GetTabDefinitions();
            var tabs = tabKeys.Where(x => conf.Tabs.Contains(x.Key)).ToList();

            var dto = new ConfigDto
            {
                DeviceId = deviceId.FullId,
                DeviceName = deviceId.DeviceName,
                User = deviceId.User,
                Reconfigure = true,

                Info = conf.Info,
                Tabs = tabs
//                Tabs = GetDeviceTabs(deviceId)
            };
            return dto;
        }

        public DeviceInfoDto GetDeviceInfo(DeviceId deviceId)
        {
            var info = _deviceInfoHandler.GetDeviceInfo(deviceId);

            if (info == null)
            {
                return new DeviceInfoDto()
                {
                    DeviceId = deviceId.FullId
                };
            }

            var dto = new DeviceInfoDto()
            {
                DeviceId = deviceId.FullId,
                DeviceName = info.DeviceName,
                FcmToken = info.FcmToken,
                Type = info.Type,
                Family = info.family,
                AdditionalProperties = info.properties
            };
            return dto;
        }

        public DeviceInfoDto UpdateDeviceInfo(DeviceId deviceId, DeviceInfo deviceInfo)
        {
            _deviceInfoHandler.UpdateDeviceInfo(deviceId, deviceInfo);
            return GetDeviceInfo(deviceId);
        }

        public ServerInfo GetServerInfo()
        {
            var app = _configHandler.GetAppConfig();
            return new ServerInfo
            {
                AuthServer = app.AuthServer,
                ServerVersion = app.ServerVersion
            };
        }
    }
}
