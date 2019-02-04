//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;

namespace SmartDevicesGateway.Model.Messages
{
    public class MessageReply : Message
    {
        public string Action { get; set; }

        [JsonExtensionData]
        public Dictionary<string, object> AdditionalProperties { get; set; } = new Dictionary<string, object>();

        public MessageReply()
        {
            Type = "JobReply";
        }
    }
}
