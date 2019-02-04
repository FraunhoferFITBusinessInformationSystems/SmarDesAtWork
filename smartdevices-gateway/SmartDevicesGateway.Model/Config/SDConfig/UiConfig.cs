//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using SmartDevicesGateway.Common.Interfaces;
using SmartDevicesGateway.Model.Config.ValueSpecifications;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Tabs;
using SmartDevicesGateway.Model.Ui;

namespace SmartDevicesGateway.Model.Config.SDConfig
{
    public class UiConfig : IConfig
    {
        public AppInfo Info { get; set; }

        public IEnumerable<UiLayout> Uis { get; set; }

        public IEnumerable<ConfigValueSpecification> ValueDefinitions { get; set; }

        public IEnumerable<UiAction> ActionDefinitions { get; set; }

        public IEnumerable<TabConfig> TabDefinitions { get; set; }
    }
}
