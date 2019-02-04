//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Runtime.Serialization;

namespace SmartDevicesGateway.Processing.Exceptions.Base
{
    public abstract class SmartDevicesGatewayException : Exception
    {
        protected SmartDevicesGatewayException()
        {
        }

        protected SmartDevicesGatewayException(string message) : base(message)
        {
        }

        protected SmartDevicesGatewayException(string message, Exception innerException) : base(message, innerException)
        {
        }

        protected SmartDevicesGatewayException(SerializationInfo info, StreamingContext context) : base(info, context)
        {
        }
    }
}
