//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
namespace SmartDevicesGateway.Model.Amqp
{
    public class AmqpFilterRequest
    {
        public string Name { get; set; }
        public string Value { get; set; }
        public bool Excluding { get; set; } = false;
    }
}