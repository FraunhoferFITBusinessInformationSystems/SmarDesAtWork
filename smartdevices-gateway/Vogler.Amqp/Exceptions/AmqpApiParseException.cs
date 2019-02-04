//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;

namespace Vogler.Smartdevices.Common.Amqp
{
    public class AmqpApiParseException : Exception
    {
        public AmqpApiParseException()
        {
        }

        public AmqpApiParseException(string message) : base(message)
        {
        }

        public AmqpApiParseException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
