//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//

using System.Collections.Generic;
using Newtonsoft.Json;
using SmartDevicesGateway.Model.Enums;

namespace SmartDevicesGateway.Model.Internal
{
    public class DeviceInfo
    {
        public string Id { get; set; }
        public string DeviceName { get; set; }

        public DeviceType Type { get; set; }
        public DeviceFamily family { get; set; }

        public string FcmToken { get; set; }

        //[NotMapped]
        //[JsonExtensionData]
        public Dictionary<string, object> properties { get; set; }
    }
}
