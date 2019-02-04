//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;

namespace SmartDevicesGateway.Model.Notification
{
    [JsonObject(ItemNullValueHandling = NullValueHandling.Ignore)]
    public class FcmMessageBody
    {
        public static class Actions
        {
            public const string GetData = "GetData";
            public const string GetConfig = "GetConfig";
            public const string GetAll = "GetAll";
        }

        public FcmMessageBody()
        {
        }

        public FcmMessageBody(string action, bool notification, int pattern)
        {
            Action = action;
            Notification = notification;
            Pattern = pattern;
        }

        [JsonProperty("action")]
        public string Action { get; set; } = Actions.GetData;

        [JsonProperty("notification")]
        public bool Notification { get; set; }

        [JsonProperty("pattern")]
        public int? Pattern { get; set; }
    }
}
