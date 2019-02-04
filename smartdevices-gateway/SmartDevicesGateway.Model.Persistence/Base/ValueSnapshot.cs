//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;

namespace SmartDevicesGateway.Model.Persistence.Base
{
    public class ValueSnapshot<T>
    {
        public T Value { get; set; }
        public DateTimeOffset Date { get; set; }
    }
}