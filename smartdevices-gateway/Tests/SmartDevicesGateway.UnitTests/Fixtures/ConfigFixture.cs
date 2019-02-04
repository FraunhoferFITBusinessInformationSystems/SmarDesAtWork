//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.IO;
using SmartDevicesGateway.Common;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Services;
using SmartDevicesGateway.Services.ConfigService;

namespace SmartDevicesGateway.UnitTests.Fixtures
{
    public class ConfigFixture : LoggingFixture
    {
        public ConfigService ConfigService { get; }

        public ConfigFixture()
        {
            ConfigService = new ConfigService(LoggerFactory){ ContentRoot = ContentRoot };
            ConfigService.RegisterConfigFile(Path.Combine(ContentRoot, "config/ServiceConfig.json"), typeof(ServiceConfig));
            ConfigService.RegisterConfigFile(Path.Combine(ContentRoot, "config/SmartDeviceConfig.json"), typeof(SmartDeviceConfig));
            ConfigService.RegisterConfigFile(Path.Combine(ContentRoot, "config/UserConfig.json"), typeof(UserConfig));
            ConfigService.RegisterConfigFile(Path.Combine(ContentRoot, "config/AppConfig.json"), typeof(AppConfig));
            ConfigService.RegisterConfigFile(Path.Combine(ContentRoot, "config/UiConfig.json"), typeof(UiConfig));
        }
    }
}
