//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.Model.Dto
{
    public class ServerInfo
    {
        public string AuthServer { get; set; }
        public string ServerVersion { get; set; }
        private IEnumerable<string> Endpoints { get; set; }
    }
}
