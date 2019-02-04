//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Ui;

namespace SmartDevicesGateway.Model.Internal
{
    public class DeviceConfig
    {
        public AppInfo Info { get; set; }
        public int UpdateInterval { get; set; }
        public DeviceId DeviceId { get; set; }
        public User User { get; set; }

        public IEnumerable<DeviceGroup> DeviceGroups { get; set; }

        public ISet<string> Values { get; set; }

        public ISet<string> Actions { get; set; }

        public ISet<string> Tabs { get; set; }
        public SmartDevice Device { get; set; }
    }
}
