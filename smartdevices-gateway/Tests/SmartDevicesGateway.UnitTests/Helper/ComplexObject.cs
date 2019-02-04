//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;

namespace SmartDevicesGateway.UnitTests //todo change ns
{
    public class ComplexObject : IEquatable<ComplexObject>
    {
        public ComplexObject()
        {            
            Identifier = "testIdentifier";
            Object1 = new SimpleObject();
            Dictionary1 = new Dictionary<string, SimpleObject>()
            {
                { "Dictionary1Key", new SimpleObject() }
            };
        }

        public string Identifier { get; set; }
        public SimpleObject Object1 { get; set; }
        public Dictionary<string, SimpleObject> Dictionary1 { get; set; }

        public bool Equals(ComplexObject other)
        {
            bool dictMatches = Dictionary1.Count == other.Dictionary1.Count;
            
            foreach (var expectedKeyPair in Dictionary1)
            {
                if (! other.Dictionary1[expectedKeyPair.Key].Equals(expectedKeyPair.Value))
                {
                    dictMatches = false;
                }
            }

            return
                Identifier == other.Identifier
                && Object1 == other.Object1
                && dictMatches;
        }
    }
}
