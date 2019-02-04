//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using Newtonsoft.Json;

namespace SmartDevicesGateway.Services.FcmService.Responses
{
    /// <summary>
    /// This class represents the Firebase Cloud Messaging Downstream HTTP FcmMessage Response and can be easily parsed to JSON. 
    /// The Descriptions and Documentation is copied from Google Cloud Messaging Documentation unter <see cref="https://developers.google.com/cloud-messaging/http-server-ref"/>
    /// </summary>
    public class DownstreamMessageResponse
    {
        /// <summary>
        /// Unique ID (number) identifying the multicast message.
        /// </summary>
        /// <value>Required, number, PropertyName: "multicast_id"</value>
        [JsonProperty(PropertyName = "multicast_id", NullValueHandling = NullValueHandling.Include)]
        public long MulticastId { get; set; }

        /// <summary>
        /// Number of messages that were processed without an error.
        /// </summary>
        /// <value>Required, number, PropertyName: "success"</value>
        [JsonProperty(PropertyName = "success", NullValueHandling = NullValueHandling.Include)]
        public int Success { get; set; }

        /// <summary>
        /// Number of messages that could not be processed.
        /// </summary>
        /// <value>Required, number, PropertyName: "failure"</value>
        [JsonProperty(PropertyName = "failure", NullValueHandling = NullValueHandling.Include)]
        public int Failure { get; set; }

        /// <summary>
        /// Number of results that contain a canonical registration token. See the registration overview for more discussion of this topic.
        /// </summary>
        /// <value>Required, number, PropertyName: "canonical_ids"</value>
        [JsonProperty(PropertyName = "canonical_ids", NullValueHandling = NullValueHandling.Include)]
        public int CanonicalIds { get; set; }

        /// <summary>
        /// Array of objects representing the status of the messages processed. The objects are listed in the same order as the request (i.e., for each registration ID in the request, its result is listed in the same index in the response).
        /// </summary>
        /// <value>Optional, array object, PropertyName: "results"</value>
        [JsonProperty(PropertyName = "results", NullValueHandling = NullValueHandling.Include)]
        public ICollection<DownstreamMessageResult> Results { get; set; }
    }
}