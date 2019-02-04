//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Messages.Base;
using SmartDevicesGateway.Model.Persistence.Message;

namespace SmartDevicesGateway.Model.Persistence.Job
{
    public class BufferedJob : IBufferedJob
    {
        public BufferedJob()
        {
            CreateDate = DateTimeOffset.Now;
            Sent = false;
        }

        public int LiteDbId { get; set; }

        public Guid Id { get; set; }
        public Jobs.Job Job { get; set; }
        
        public bool Sent { get; set; }
        public DeviceId DeviceId { get; set; }

        public DateTimeOffset CreateDate { get; set; }
        public DateTimeOffset SentDate { get; set; }
        public DateTimeOffset ReplyDate { get; set; }

        public Dictionary<string, object> Header { get; set; }
        IReadOnlyDictionary<string, object> IBufferedJob.Header => Header;
    }
}
