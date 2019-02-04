//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Messages.Enums;

namespace SmartDevicesGateway.Model.Messages.Base
{
    public interface IMessage
    {
        /// <summary>
        /// Unique Identifier as UUID or Hash
        /// </summary>
        Guid Id { get; set; }

        Guid ReferenceId { get; set; }

        string CreatedBy { get; set; }
        DateTimeOffset CreatedAt { get; set; }
        string AssignedTo { get; set; }
        DateTimeOffset AssignedAt { get; set; }

        string Type { get; set; }
    }
}
