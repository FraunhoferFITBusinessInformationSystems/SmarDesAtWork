//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using System.ComponentModel;
using Newtonsoft.Json;

namespace SmartDevicesGateway.Services.FcmService.Requests
{
    public class FcmMessage
    {
        /// <summary>
        /// This parameter specifies the recipient of a message.
        /// The value must be a registration token, notification key, or topic.
        /// </summary>
        /// <value>Required, string, PropertyName: "to"</value>
        [JsonProperty(PropertyName = "to", NullValueHandling = NullValueHandling.Ignore)]
        public string To { get; set; }

        /// <summary>
        /// This parameter specifies a list of devices (registration tokens, or IDs) receiving a multicast message. It must contain at least 1 and at most 1000 registration tokens.
        /// Use this parameter only for multicast messaging, not for single recipients. Multicast messages (sending to more than 1 registration tokens) are allowed using HTTP JSON format only.
        /// </summary>
        /// <value>String array, PropertyName: "registration_ids"</value>
        [JsonProperty(PropertyName = "registration_ids", NullValueHandling = NullValueHandling.Ignore)]
        public ICollection<string> RegistrationIds { get; set; }

        /// <summary>
        /// This parameter identifies a group of messages (e.g., with collapse_key: "Updates Available") that can be collapsed, so that only the last message gets sent when delivery can be resumed. This is intended to avoid sending too many of the same messages when the device comes back online or becomes active.
        /// Note that there is no guarantee of the order in which messages get sent.
        /// Note: A maximum of 4 different collapse keys is allowed at any given time.This means a GCM connection server can simultaneously store 4 different send-to-sync messages per client app.If you exceed this number, there is no guarantee which 4 collapse keys the GCM connection server will keep.
        /// </summary>
        /// <value>Optional, string, PropertyName: "collapse_key"</value>
        [JsonProperty(PropertyName = "collapse_key", NullValueHandling = NullValueHandling.Ignore)]
        public string CollapseKey { get; set; }

        /// <summary>
        /// Sets the priority of the message. Valid values are "normal" and "high." On iOS, these correspond to APNs priority 5 and 10.
        /// By default, notification messages are sent with high priority, and data messages are sent with normal priority.Normal priority optimizes the client app's battery consumption, and should be used unless immediate delivery is required. For messages with normal priority, the app may receive the message with unspecified delay.
        /// When a message is sent with high priority, it is sent immediately, and the app can wake a sleeping device and open a network connection to your server.
        /// </summary>
        /// <seealso href="https://developers.google.com/cloud-messaging/concept-options#setting-the-priority-of-a-message">
        /// For more information, see Setting the priority of a message.
        /// </seealso>
        /// <value>Optional, string, PropertyName: "priority"</value>
        [JsonProperty(PropertyName = "priority", NullValueHandling = NullValueHandling.Ignore)]
        public FcmMessagePriority Priority { get; set; }

        /// <summary>
        /// This parameter specifies how long (in seconds) the message should be kept in GCM storage if the device is offline. The maximum time to live supported is 4 weeks, and the default value is 4 weeks. For more information, see Setting the lifespan of a message.
        /// </summary>
        /// <seealso href="https://developers.google.com/cloud-messaging/concept-options#ttl"/>
        /// <value>Optional, JSON Number, PropertyName: "time_to_live"</value>
        [DefaultValue(4 * 7 * 24 * 60 * 60)]
        [JsonProperty(PropertyName = "time_to_live", NullValueHandling = NullValueHandling.Ignore)]
        public int? TimeToLive { get; set; }

        /// <summary>
        /// This parameter specifies the package name of the application where the registration tokens must match in order to receive the message.
        /// </summary>
        /// <value>Optional, string, PropertyName: "restricted_package_name"</value>
        [JsonProperty(PropertyName = "restricted_package_name", NullValueHandling = NullValueHandling.Ignore)]
        public string RestrictedPackageName { get; set; }

        /// <summary>
        /// This parameter, when set to true, allows developers to test a request without actually sending a message.
        /// The default value is false.
        /// </summary>
        /// <value>Optional, JSON boolean, PropertyName: "dry_run"</value>
        [JsonProperty(PropertyName = "dry_run", NullValueHandling = NullValueHandling.Ignore)]
        public bool? DryRun { get; set; }

        /// <summary>
        /// This parameter specifies the custom key-value pairs of the message's payload.
        /// For example, with data:{"score":"3x1"}:
        /// On Android, this would result in an intent extra named score with the string value 3x1.
        /// On iOS, if the message is sent via APNS, it represents the custom data fields.If it is sent via GCM connection server, it would be represented as key value dictionary in AppDelegate application:didReceiveRemoteNotification:.
        /// The key should not be a reserved word ("from" or any word starting with "google" or "gcm"). Do not use any of the words defined in this table(such as collapse_key).
        /// Values in string types are recommended.You have to convert values in objects or other non-string data types (e.g., integers or booleans) to string.
        /// </summary>
        /// <value>Optional, JSON object, PropertyName: "data"</value>
        [JsonProperty(PropertyName = "data", NullValueHandling = NullValueHandling.Ignore)]
        public object Data { get; set; }

        /// <summary>
        /// This parameter specifies the predefined, user-visible key-value pairs of the notification payload. See Notification payload support for detail. For more information about notification message and data message options, see Payload.
        /// </summary>
        /// <seealso href="https://developers.google.com/cloud-messaging/concept-options#notifications_and_data_messages"/>
        /// <value>Optional, JSON object, PropertyName: "notification"</value>
        [JsonProperty(PropertyName = "notification", NullValueHandling = NullValueHandling.Ignore)]
        public FcmNotification FcmNotification { get; set; }

        public FcmMessage()
        {
            DryRun = false;
            TimeToLive = 4 * 7 * 24 * 60 * 60;
        }
    }
}