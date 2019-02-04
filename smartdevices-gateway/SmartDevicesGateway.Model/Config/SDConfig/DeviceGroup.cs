//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Config.SDConfig.Tabs;
using SmartDevicesGateway.Model.Dto.Config;

namespace SmartDevicesGateway.Model.Config.SDConfig
{
    public class DeviceGroup
    {
        public string GroupName { get; set; }

        public LiveFeedTabConfig LiveFeed { get; set; }
        public DashboardTabConfig Dashboard { get; set; }
        
        public IEnumerable<string> VisibleTabs { get; set; }
    }
}
