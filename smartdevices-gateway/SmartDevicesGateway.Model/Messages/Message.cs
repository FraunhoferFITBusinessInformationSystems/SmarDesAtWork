//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Messages.Base;
using SmartDevicesGateway.Model.Messages.Enums;

namespace SmartDevicesGateway.Model.Messages
{
    public class Message : IMessage
    {
        public Guid Id { get; set; }
        public Guid ReferenceId { get; set; }
        public MessagePriority Priority { get; set; }

        public string Name { get; set; }

        public string CreatedBy { get; set; }
        public DateTimeOffset CreatedAt { get; set; }
        public string AssignedTo { get; set; }
        public DateTimeOffset AssignedAt { get; set; }

        public string Type { get; set; }
    }
}