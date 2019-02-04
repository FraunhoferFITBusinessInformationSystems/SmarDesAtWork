//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.Model.Config.SDConfig
{
    public class User
    {
        public string Username { get; set; }
        public string Pw { get; set; }

        public string FullName { get; set; }

        public IEnumerable<string> Groups { get; set; }

        public IEnumerable<SmartDevice> Devices { get; set; }
        
    }
}
