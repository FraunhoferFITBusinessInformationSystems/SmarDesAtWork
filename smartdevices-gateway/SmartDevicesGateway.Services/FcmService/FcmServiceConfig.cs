//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Services.ConfigService;

namespace SmartDevicesGateway.Services.FcmService
{
    public class FcmServiceConfig
    {
        public string ApiUrl { get; set; }
        public string ServerKey { get; set; }
    }
}
