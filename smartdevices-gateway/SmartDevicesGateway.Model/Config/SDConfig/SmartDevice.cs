//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Enums;

namespace SmartDevicesGateway.Model.Config.SDConfig
{
    public class SmartDevice
    {
        public string DeviceName { get; set; }

        public DeviceFamily DeviceFamily { get; set; }

        public DeviceType DeviceType { get; set; }
        
        public IEnumerable<string> DeviceGroups { get; set; }
    }
}
