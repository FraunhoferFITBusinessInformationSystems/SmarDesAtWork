//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace SmartDevicesGateway.Model.Enums
{
    [JsonConverter(typeof(StringEnumConverter))]
    public enum DeviceFamily
    {
        Unknown = 0,
        Phone = 1,
        Tablet = 2,
        Watch = 4,
        HoloLens = 8,
    }
}
