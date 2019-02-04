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
    public class UnknownKeyException : SmartDevicesGatewayException
    {
        public UnknownKeyException()
        {
        }

        public UnknownKeyException(string message) : base(message)
        {
        }

        public UnknownKeyException(string message, Exception innerException) : base(message, innerException)
        {
        }

        public UnknownKeyException(SerializationInfo info, StreamingContext context) : base(info, context)
        {
        }
    }
}
