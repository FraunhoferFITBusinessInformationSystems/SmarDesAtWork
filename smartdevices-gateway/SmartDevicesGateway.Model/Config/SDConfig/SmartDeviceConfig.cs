//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Common.Interfaces;
using SmartDevicesGateway.Model.Config.ValueSpecifications;
using SmartDevicesGateway.Model.Ui;

namespace SmartDevicesGateway.Model.Config.SDConfig
{
    public class SmartDeviceConfig : IConfig
    {
        public IEnumerable<DeviceGroup> DeviceGroups { get; set; }
        public IEnumerable<ConfigValueSpecification> ValueDefinitions { get; set; }
        public IEnumerable<UiLayout> Uis { get; set; }
    }
}
