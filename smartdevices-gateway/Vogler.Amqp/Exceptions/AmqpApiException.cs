//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace Vogler.Smartdevices.Common.Amqp
{
    public class AmqpApiException : Exception
    {
        public AmqpApiException()
        {
        }

        public AmqpApiException(string message) : base(message)
        {
        }

        public AmqpApiException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
