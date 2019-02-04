//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.Common.Extensions
{
    public static class IntExtensions
    {
        public static void Repeat(
            this int count,
            Action action
        )
        {
            for (var x = 0; x < count; x += 1)
            {
                action();
            }
        }

        public static void Repeat(
            this int count,
            Action<int> action
        )
        {
            for (var x = 0; x < count; x += 1)
            {
                action(x);
            }
        }

        public static IEnumerable<TResult> Repeat<TResult>(
            this int count,
            Func<int, TResult> func
        )
        {
            var list = new List<TResult>();
            for (var x = 0; x < count; x += 1)
            {
                list.Add(func.Invoke(x));
            }
            return list;
        }
    }
}
