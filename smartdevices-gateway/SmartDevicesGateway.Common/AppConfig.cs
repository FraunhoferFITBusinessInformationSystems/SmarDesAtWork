//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Common.Interfaces;

namespace SmartDevicesGateway.Common
{
    public class AppConfig : IConfig
    {
        public string ResourceDirectory { get; set; }
        public string ServerVersion { get; set; }
        public string AuthServer { get; set; }
    }
}
