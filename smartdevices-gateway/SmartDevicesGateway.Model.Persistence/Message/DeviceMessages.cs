//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;

namespace SmartDevicesGateway.Model.Persistence.Message
{
    public class DeviceMessages : IDeviceMessages
    {
        private readonly List<BufferedMessage> _messages = new List<BufferedMessage>();
        public IEnumerable<IBufferedMessage> Jobs => _messages;
        public int Count => _messages.Count;
        public int CountNotSent => _messages.Count(x => !x.Sent);
        public IBufferedMessage GetMessage(Guid messageId)
        {
            return _messages.Find((x => x.Id == messageId));
        }

        public bool HasMessage(Guid messageId)
        {
            return _messages.Any(x => x.Id == messageId);
        }

        public void Add(BufferedMessage bufferedMessage)
        {
            _messages.Add(bufferedMessage);
        }
    }
}