//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Messages.Base;

namespace SmartDevicesGateway.Model.Persistence.Message
{
    public class BufferedMessage : IBufferedMessage
    {
        public BufferedMessage()
        {
            CreateDate = DateTimeOffset.Now;
            Sent = false;
        }

        public Guid Id { get; set; }
        public IMessage Message { get; set; }
        public bool Sent { get; set; }
        public DeviceId DeviceId { get; set; }

        public DateTimeOffset CreateDate { get; set; }
        public DateTimeOffset SentDate { get; set; }
        public DateTimeOffset ReplyDate { get; set; }

        public Dictionary<string, object> Header { get; set; }
        IReadOnlyDictionary<string, object> IBufferedMessage.Header => Header;
    }
}
