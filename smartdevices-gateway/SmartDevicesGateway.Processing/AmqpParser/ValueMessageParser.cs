//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using Amqp;
using SmartDevicesGateway.Model.Amqp;
using SmartDevicesGateway.Processing.AmqpParser.Base;
using SmartDevicesGateway.Processing.Exceptions;

namespace SmartDevicesGateway.Processing.AmqpParser
{
    public class ValueMessageParser : AbstractMessageParser<MwValue>
    {
        private const string ValueMessageTypeName = "ValueData";

        public override bool IsResponsible(Message message)
        {
            var map = message.ApplicationProperties.Map;
            if (!map.ContainsKey("Type"))
            {
                return false;
            }

            var t = (string)map["Type"];
            return t.Equals(ValueMessageTypeName, StringComparison.OrdinalIgnoreCase);
        }

        public override MwValue ParseMessage(Message message)
        {
            var props = message.ApplicationProperties.Map;

            var ns = props["Namespace"] as string;
            var name = props["Name"] as string;
            var val = props["Value"];

            if (name == null || ns == null)
            {
                throw new MessageParsingException("Couldn't read ValueMessage.");
            }

            return new MwValue()
            {
                Namespace = ns,
                Name = name,
                Value = val
            };
        }
    }
}