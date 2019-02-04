//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Messages;

namespace SmartDevicesGateway.Model.Jobs
{
    public class Job : Message
    {
        public int LiteDbId { get; set; }

        public Job()
        {
            //for liteDB
        }

        public bool Immediate { get; set; }

        public Dictionary<string, string> Resource { get; set; }

        public JobStatus Status { get; set; }
    }

    public class JobReply : Message
    {
        public string Action { get; set; }

        public Dictionary<string, string> Resource { get; set; }

    }
}