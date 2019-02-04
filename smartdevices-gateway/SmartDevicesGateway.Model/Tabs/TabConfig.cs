//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using Newtonsoft.Json;
using SmartDevicesGateway.Model.Enums;

namespace SmartDevicesGateway.Model.Tabs
{
    public class TabConfig
    {
        public string Title { get; set; }
        public string Key { get; set; }
        public bool MainTab { get; set; } = false;

        public bool ShowBadgeNumber { get; set; } = false;
        public AppDrawables Icon { get; set; }
        public IEnumerable<TabSortEntry> SortEntries { get; set; }
        public IEnumerable<TabFilterEntry> FilterEntries { get; set; }

        [JsonExtensionData]
        public Dictionary<string, object> AdditionalProperties { get; set; } = new Dictionary<string, object>();
    }
}