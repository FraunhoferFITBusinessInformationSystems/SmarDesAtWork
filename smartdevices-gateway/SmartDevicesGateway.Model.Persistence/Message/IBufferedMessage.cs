//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Messages.Base;
using SmartDevicesGateway.Model.Persistence.Base;

namespace SmartDevicesGateway.Model.Persistence.Message
{
    public interface IBufferedMessage
    {
        IMessage Message { get; }
        Guid Id { get; }
        bool Sent { get; }
        DeviceId DeviceId { get; }
        IReadOnlyDictionary<string, object> Header { get; }
        DateTimeOffset CreateDate { get; }
        DateTimeOffset SentDate { get; }
        DateTimeOffset ReplyDate { get; }
    }
}
