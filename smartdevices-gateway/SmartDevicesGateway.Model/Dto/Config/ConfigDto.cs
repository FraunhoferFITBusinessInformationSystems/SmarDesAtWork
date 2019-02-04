//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Tabs;
using SmartDevicesGateway.Model.Ui;

namespace SmartDevicesGateway.Model.Dto.Config
{
    public class ConfigDto
    {

        public AppInfo Info { get; set; }
        public string DeviceId { get; set; }

        public string DeviceName { get; set; }
        public string User { get; set; }
        public string DataUrl { get; set; }

        public IEnumerable<TabConfig> Tabs { get; set; }

        public bool Reconfigure { get; set; }
    }
}
