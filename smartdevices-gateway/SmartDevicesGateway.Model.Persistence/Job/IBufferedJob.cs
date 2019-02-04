//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using SmartDevicesGateway.Model.Dto.Config;

namespace SmartDevicesGateway.Model.Persistence.Job
{
    public interface IBufferedJob
    {
        Jobs.Job Job { get; }
        Guid Id { get; }
        bool Sent { get; }
        DeviceId DeviceId { get; }
        IReadOnlyDictionary<string, object> Header { get; }
        DateTimeOffset CreateDate { get; }
        DateTimeOffset SentDate { get; }
        DateTimeOffset ReplyDate { get; }
    }
}
