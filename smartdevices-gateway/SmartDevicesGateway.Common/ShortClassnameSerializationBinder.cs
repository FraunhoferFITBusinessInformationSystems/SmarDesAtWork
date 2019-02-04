//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json.Serialization;

namespace SmartDevicesGateway.Common
{
    public class ShortClassnameSerializationBinder : ISerializationBinder
    {
        private readonly DefaultSerializationBinder _DefaultSerializationBinder = new DefaultSerializationBinder();

        public Type BindToType(string assemblyName, string typeName)
        {
            return _DefaultSerializationBinder.BindToType(assemblyName, typeName);
        }

        public void BindToName(Type serializedType, out string assemblyName, out string typeName)
        {
            assemblyName = null;
            typeName = serializedType.Name;
        }
    }
}
