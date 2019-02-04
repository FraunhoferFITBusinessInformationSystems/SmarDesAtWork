//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.Model.Config.SDConfig.Tabs
{
    public class LiveFeedTabConfig
    {
        public int DataUpdateInterval { get; set; }
        public IEnumerable<string> Values { get; set; }
    }
}
