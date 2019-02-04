//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;

namespace SmartDevicesGateway.UnitTests //todo change ns
{
    public class SimpleObject : IEquatable<SimpleObject>
    {
        public SimpleObject()
        {
            Name = "testName";
            Int32 = 42;
        }

        public string Name { get; set; }
        public Int32 Int32 { get; set; }

        public bool Equals(SimpleObject other)
        {
            return
                Name == other.Name
                && Int32 == other.Int32;
        }
    }
}
