//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace SmartDevicesGateway.Common.Extensions
{
    public static class WorkaroundExtensions
    {
        public static Dictionary<string, T> ConvertToLowerCamelCaseKeys<T>(this Dictionary<string, T> dict)
        {
            return dict.Select(t => new { Key = t.Key.ToFirstCharLowerCase(), Value = t.Value })
                .ToDictionary(t => t.Key, t => t.Value);
        }
    }
}
