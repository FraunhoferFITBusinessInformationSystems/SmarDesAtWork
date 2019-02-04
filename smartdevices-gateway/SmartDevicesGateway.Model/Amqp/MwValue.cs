//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Model.Values;

namespace SmartDevicesGateway.Model.Amqp
{
    public class MwValue
    {
        public string Name { get; set; }
        public string Namespace { get; set; }
        public object Value { get; set; }

        public Value ConvertToValue()
        {
            return ConvertToValue(this);
        }

        public static Value ConvertToValue(MwValue mwValue)
        {
            return new Value
            {
                Val = mwValue.Value,
                Name = mwValue.Namespace + ":" + mwValue.Name
            };
        }
    }
}
