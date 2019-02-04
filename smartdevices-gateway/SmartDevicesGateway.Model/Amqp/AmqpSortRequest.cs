//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.Model.Amqp
{
    public class AmqpSortRequest
    {
        public string Name { get; set; }

        public bool Ascending { get; set; }
    }
}
