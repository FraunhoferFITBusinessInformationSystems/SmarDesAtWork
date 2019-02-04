//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.Common.Extensions
{
    public static class StringExtensions
    {
        public static string ToFirstCharLowerCase(this string str)
        {
            return string.IsNullOrEmpty(str) 
                ? null : str[0].ToString().ToLower()
                         + ((str.Length >= 1) ? str.Substring(1) : "");
        }

        public static string ToFirstCharUpperCase(this string str)
        {
            return string.IsNullOrEmpty(str)
                ? null : str[0].ToString().ToUpper()
                         + ((str.Length >= 1) ? str.Substring(1) : "");
        }
    }
}
