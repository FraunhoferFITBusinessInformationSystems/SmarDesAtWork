//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//

// ReSharper disable once CheckNamespace
namespace System
{
    public static class EnumExtensions
    {
        public static string ToLower(this Enum enumerable)
        {
            return enumerable?.ToString()?.ToLowerInvariant();
        }


        public static string Remove(this Enum enumerable, string text)
        {
            return enumerable?.ToString()?.Replace(text, "");
        }
    }
}
