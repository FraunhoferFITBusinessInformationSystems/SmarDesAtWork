//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.ComponentModel;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace SmartDevicesGateway.Services.FcmService.Responses
{
    public class TopicMessageResponse
    {
        /// <summary>
        /// The topic message ID when GCM has successfully received the request and will attempt to deliver to all subscribed devices.
        /// </summary>
        /// <value>Optional, number, PropertyName: "message_id"</value>
        [JsonProperty(PropertyName = "message_id", NullValueHandling = NullValueHandling.Include)]
        public string MessageId { get; set; }

        /// <summary>
        /// String specifying the error that occurred when processing the message for the recipient. The possible values can be found reference.
        /// </summary>
        /// <see cref="https://developers.google.com/cloud-messaging/http-server-ref#table9"/>
        /// <value>Optional, string, PropertyName: "error"</value>
        [JsonConverter(typeof(StringEnumConverter))]
        [JsonProperty("error", NullValueHandling = NullValueHandling.Ignore, DefaultValueHandling = DefaultValueHandling.Populate)]
        public ResponseError Error { get; set; }

        [JsonIgnore] public bool HasErrors => Error != ResponseError.NoError;
    }
}
