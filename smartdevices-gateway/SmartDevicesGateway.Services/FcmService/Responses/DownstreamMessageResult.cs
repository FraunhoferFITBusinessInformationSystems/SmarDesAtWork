//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using Newtonsoft.Json;

namespace SmartDevicesGateway.Services.FcmService.Responses
{
    public class DownstreamMessageResult : TopicMessageResponse
    {
        /// <summary>
        /// Optional string specifying the canonical registration token for the client app that the message was processed and sent to. Sender should use this value as the registration token for future requests. Otherwise, the messages might be rejected.
        /// </summary>
        /// <value>string, PropertyName: "registration_id"</value>
        [JsonProperty(PropertyName = "registration_id", NullValueHandling = NullValueHandling.Include)]
        public string RegistrationId { get; set; }
    }
}