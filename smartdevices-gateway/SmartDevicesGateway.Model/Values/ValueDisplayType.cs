﻿//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace SmartDevicesGateway.Model.Values.Enums
{
    [JsonConverter(typeof(StringEnumConverter))]
    public enum ValueDisplayType
    {
        ValueDisplayPlain,
        ValueDisplayGauge,
        ValueDisplayText
    }
}
