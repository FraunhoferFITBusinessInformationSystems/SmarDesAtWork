//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace SmartDevicesGateway.Model.Amqp.Enums
{
    [JsonConverter(typeof(StringEnumConverter))]
    public enum ResourceAction
    {
        Create,
        CreateMany,
        Read,
        ReadMany,
        Update,
        UpdateMany,
        Delete,
        DeleteMany
    }
}
