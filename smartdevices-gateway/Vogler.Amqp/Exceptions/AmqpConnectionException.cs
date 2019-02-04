//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Runtime.Serialization;

namespace Vogler.Amqp
{
    public class AmqpConnectionException : Exception
    {
        public AmqpConnectionException()
        {
        }

        public AmqpConnectionException(string message) : base(message)
        {
        }

        public AmqpConnectionException(string message, Exception innerException) : base(message, innerException)
        {
        }

        protected AmqpConnectionException(SerializationInfo info, StreamingContext context) : base(info, context)
        {
        }
    }
}
