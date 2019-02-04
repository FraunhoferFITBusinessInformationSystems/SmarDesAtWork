//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Text;
using SmartDevicesGateway.Processing.Exceptions.Base;

namespace SmartDevicesGateway.Processing.Exceptions
{
    public class MessageParsingException : SmartDevicesGatewayException
    {
        public MessageParsingException()
        {
        }

        public MessageParsingException(string message) : base(message)
        {
        }

        public MessageParsingException(string message, Exception innerException) : base(message, innerException)
        {
        }

        public MessageParsingException(SerializationInfo info, StreamingContext context) : base(info, context)
        {
        }
    }
}
