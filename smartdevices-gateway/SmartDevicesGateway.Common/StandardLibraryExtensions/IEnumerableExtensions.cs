//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//

// ReSharper disable once CheckNamespace

using System.Collections.Generic;
using System.Linq;

namespace System
{
    public static class IEnumerableExtensions
    {
        public static IEnumerable<T> Except<T>(this IEnumerable<T> enumerable, T other)
        {
            return enumerable.Except<T>(new[] {other});
        }
    }
}
