//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Linq;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Processing.Exceptions.Base;
using SmartDevicesGateway.Processing.Handler;
using SmartDevicesGateway.Services.ConfigService;
using SmartDevicesGateway.Services.FcmService;

namespace SmartDevicesGateway.Processing.Controller.Config
{
    public class ConfigChangeModel : AbstractModel
    {
        private readonly ValueHandler _valueHandler;
        private readonly IConfigService _configService;
        private readonly IPersistenceProvider _persistenceProvider;
        private readonly FcmMessageHandler _fcmHandler;


        public ConfigChangeModel(ILoggerFactory loggerFactory, ValueHandler valueHandler,
            IConfigService configService, IPersistenceProvider persistenceProvider, FcmMessageHandler fcmHandler) : base(loggerFactory)
        {
            _valueHandler = valueHandler;
            _configService = configService;
            _persistenceProvider = persistenceProvider;
            _fcmHandler = fcmHandler;
        }

        public void StartListen()
        {
            var configFileReader = _configService.GetProvider<SmartDeviceConfig>();
            var userConfigReader = _configService.GetProvider<UserConfig>();
            var uiConfigReader = _configService.GetProvider<UiConfig>();

            //Initial update
            OnSDConfigChangedEvent(this, new ConfigChangedEventArgs<SmartDeviceConfig>
            {
                OldValue = null,
                NewValue = configFileReader.Get()
            });
            configFileReader.ConfigChangedEvent += OnSDConfigChangedEvent;

            userConfigReader.ConfigChangedEvent += OnUserConfigChangedEvent;
            uiConfigReader.ConfigChangedEvent += OnUiConfigChangedEvent;

        }

        private void OnUserConfigChangedEvent(object sender, ConfigChangedEventArgs<UserConfig> e)
        {
            NotifySmartDevicesForConfigChange();
        }

        private void OnUiConfigChangedEvent(object sender, ConfigChangedEventArgs<UiConfig> e)
        {
            NotifySmartDevicesForConfigChange();
        }

        private void OnSDConfigChangedEvent(object sender,
            ConfigChangedEventArgs<SmartDeviceConfig> configChangedEventArgs)
        {
            //Update methods for all handler and services
            var valueSpecifications = configChangedEventArgs?.NewValue?.ValueDefinitions?.ToList();
            var sources = valueSpecifications?.Select(x => x.DataSource).ToList();

            if (sources != null)
            {
                _valueHandler.UpdateDataSources(sources);
            }

            if (configChangedEventArgs?.OldValue != null)
            {
                NotifySmartDevicesForConfigChange();
            }
        }

        private void NotifySmartDevicesForConfigChange()
        {
            var infos = _persistenceProvider.GetAllDeviceIds().ToArray();
            _fcmHandler.SendGetConfig(infos);
        }

        public void StopListen()
        {
            var configFileReader = _configService.GetProvider<SmartDeviceConfig>();
            configFileReader.ConfigChangedEvent -= OnSDConfigChangedEvent;
        }
    }
}