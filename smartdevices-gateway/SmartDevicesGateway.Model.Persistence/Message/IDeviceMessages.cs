//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;

namespace SmartDevicesGateway.Model.Persistence.Message
{
    public interface IDeviceMessages
    {
        IEnumerable<IBufferedMessage> Jobs { get; }

        int Count { get; }

        int CountNotSent { get; }

        IBufferedMessage GetMessage(Guid messageId);

        bool HasMessage(Guid messageId);
    }
}