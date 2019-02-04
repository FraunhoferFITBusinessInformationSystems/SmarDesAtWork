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
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Config.ValueSpecifications;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Tabs;
using SmartDevicesGateway.Model.Ui;
using SmartDevicesGateway.Processing.Exceptions;
using SmartDevicesGateway.Services.ConfigService;

namespace SmartDevicesGateway.Processing.Handler
{
    public class ConfigHandler
    {
        private readonly ILogger _logger;
        private readonly IConfigService _configService;

        private UserConfig _userConfig = null;
        private SmartDeviceConfig _smartDeviceConfig = null;
        private UiConfig _uiConfig = null;
        private AppConfig _appConfig = null;

        private SmartDeviceConfig SmartDeviceConfig
        {
            get
            {
                if (_smartDeviceConfig == null)
                {
                    _smartDeviceConfig = _configService.Get<SmartDeviceConfig>();
                }

                if (_smartDeviceConfig == null)
                {
                    _logger.LogError("Could not load SmardDevice-Configuration");
                }

                return _smartDeviceConfig;
            }
        }

        private UserConfig UserConfig
        {
            get
            {
                if (_userConfig == null)
                {
                    _userConfig = _configService.Get<UserConfig>();
                }

                if(_userConfig == null)
                {
                    _logger.LogError("Could not load User-Configuration");
                }

                return _userConfig;
            }
        }

        private UiConfig UiConfig
        {
            get
            {
                if (_uiConfig == null)
                {
                    _uiConfig = _configService.Get<UiConfig>();
                }

                if (_uiConfig == null)
                {
                    _logger.LogError("Could not load Ui-Configuration");
                }

                return _uiConfig;
            }
        }

        private AppConfig AppConfig
        {
            get
            {
                if (_appConfig == null)
                {
                    _appConfig = _configService.Get<AppConfig>();
                }

                if (_appConfig == null)
                {
                    _logger.LogError("Could not load Ui-Configuration");
                }

                return _appConfig;
            }
        }

        public ConfigHandler(ILoggerFactory loggerFactory, IConfigService configService)
        {
            _logger = loggerFactory.CreateLogger<ConfigHandler>();
            _configService = configService;

            configService.GetProvider<SmartDeviceConfig>().ConfigChangedEvent += OnConfigChangedEvent;
            configService.GetProvider<UserConfig>().ConfigChangedEvent += OnConfigChangedEvent;
            configService.GetProvider<UiConfig>().ConfigChangedEvent += OnConfigChangedEvent;
            configService.GetProvider<AppConfig>().ConfigChangedEvent += OnConfigChangedEvent;
        }

        private void OnConfigChangedEvent(object sender, ConfigChangedEventArgs<AppConfig> configChangedEventArgs)
        {
            _appConfig = configChangedEventArgs.NewValue;
        }

        private void OnConfigChangedEvent(object sender, ConfigChangedEventArgs<UiConfig> configChangedEventArgs)
        {
            _uiConfig = configChangedEventArgs.NewValue;
        }

        private void OnConfigChangedEvent(object sender, ConfigChangedEventArgs<UserConfig> configChangedEventArgs)
        {
            _userConfig = configChangedEventArgs.NewValue;
        }

        private void OnConfigChangedEvent(object sender, ConfigChangedEventArgs<SmartDeviceConfig> configChangedEventArgs)
        {
            _smartDeviceConfig = configChangedEventArgs.NewValue;
        }

        public DeviceConfig GetDeviceConfig(DeviceId deviceId)
        {
            var user = UserConfig.Users.FirstOrDefault(x => x.Username.Equals(deviceId.User));
            if (user == null)
            {
                throw new UnknownDeviceIdException($"Unknown user: \"{deviceId.User}\"");
            }

            var device = user.Devices.FirstOrDefault(x => x.DeviceName.Equals(deviceId.DeviceName));
            if (device == null)
            {
                throw new UnknownDeviceIdException($"Unknown device name: \"{deviceId.DeviceName}\"");
            }

            //merge deviceGroups and user groups in one device config
            var groups = SmartDeviceConfig.DeviceGroups.Where(x => 
                    device.DeviceGroups.Contains(x.GroupName, StringComparer.Ordinal) || 
                    user.Groups.Contains(x.GroupName, StringComparer.Ordinal))
                .ToList();

            var dashboardActions = new HashSet<string>();
            var values = new HashSet<string>();
            var tabKeys = new HashSet<string>();
            var updateInterval = int.MaxValue;

            foreach (var deviceGroup in groups)
            {
                deviceGroup.Dashboard?.Actions?.ForEach(x => dashboardActions.Add(x));
                deviceGroup.VisibleTabs?.ForEach(x => tabKeys.Add(x));

                if (deviceGroup.LiveFeed == null)
                {
                    continue;
                }

                deviceGroup.LiveFeed.Values?.ForEach(x => values.Add(x));
                if (deviceGroup.LiveFeed.DataUpdateInterval < updateInterval)
                {
                    updateInterval = deviceGroup.LiveFeed.DataUpdateInterval;
                }
            }

            var deviceConfig = new DeviceConfig
            {
                Actions = dashboardActions,
                DeviceGroups = groups,
                DeviceId = deviceId,
                UpdateInterval = updateInterval,
                User = user,
                Device = device,
                Values = values,
                Tabs = tabKeys,
                Info = UiConfig.Info
            };
            return deviceConfig;
        }

        public IEnumerable<ConfigValueSpecification> GetValueSpecifications(IEnumerable<string> values)
        {
            return values
                .Select(x => UiConfig.ValueDefinitions.First(y => y.Name.Equals(x)))
                .ToList();
        }

        public IEnumerable<ConfigValueSpecification> GetValueSpecifications()
        {
            return UiConfig.ValueDefinitions;
        }

        public IEnumerable<TabConfig> GetTabDefinitions()
        {
            return UiConfig.TabDefinitions;
        }

        public IEnumerable<UiLayout> GetUiLayouts()
        {
            return UiConfig.Uis.ToList();
        }

        public IEnumerable<UiAction> GetActions(IEnumerable<string> actionKeys)
        {
            return actionKeys
                .Select(x => UiConfig.ActionDefinitions
                    .First(y => y.Id.Equals(x)))
                .ToList();
        }

        public AppConfig GetAppConfig()
        {
            return AppConfig;
        }
    }
}
