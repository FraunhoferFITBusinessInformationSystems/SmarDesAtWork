//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//

using System.Collections.Generic;
using Newtonsoft.Json;
using SmartDevicesGateway.Model.Enums;

namespace SmartDevicesGateway.Model.Dto.Config
{
    public class DeviceInfoDto
    {
        public string DeviceId { get; set; }
        public DeviceType Type { get; set; }
        public DeviceFamily Family { get; set; }

        public string DeviceName { get; set; }

        public string FcmToken { get; set; }

        [JsonExtensionData]
        public Dictionary<string, object> AdditionalProperties { get; set; }

    }
}
