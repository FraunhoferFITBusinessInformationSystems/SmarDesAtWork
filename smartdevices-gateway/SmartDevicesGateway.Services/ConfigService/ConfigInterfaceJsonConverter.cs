//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using SmartDevicesGateway.Common.Interfaces;

namespace SmartDevicesGateway.Services.ConfigService
{
    public class ConfigInterfaceJsonConverter : Newtonsoft.Json.JsonConverter
    {
        private readonly Type _interface;
        private readonly Type[] _implementations;

        public ConfigInterfaceJsonConverter()
        {
            _interface = typeof(IConfig);
            _implementations = AppDomain.CurrentDomain.GetAssemblies()
                .SelectMany(s => s.GetTypes())
                .Where(p => _interface.IsAssignableFrom(p))
                .ToArray();
        }

        public override bool CanConvert(Type objectType)
        {
            return _implementations.Any(i => i.FullName == objectType.FullName);
        }

        public override object ReadJson(Newtonsoft.Json.JsonReader reader, Type objectType, object existingValue, Newtonsoft.Json.JsonSerializer serializer)
        {
            var targetType = _implementations.SingleOrDefault(i => i.FullName == objectType.FullName);
            if (targetType != null)
            {
                return serializer.Deserialize(reader, targetType);
            }
            throw new NotSupportedException(string.Format("Type {0} unexpected.", objectType));
        }

        public override void WriteJson(Newtonsoft.Json.JsonWriter writer, object value, Newtonsoft.Json.JsonSerializer serializer)
        {
            serializer.Serialize(writer, value);
        }
    }
}
