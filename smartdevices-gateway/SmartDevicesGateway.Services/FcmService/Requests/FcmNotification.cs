//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using Newtonsoft.Json;

namespace SmartDevicesGateway.Services.FcmService.Requests
{
    public class FcmNotification
    {
        /// <summary>
        /// Indicates notification title. This field is not visible on iOS phones and tablets.
        /// Platform: Android, iOS (Watch)
        /// </summary>
        /// <value>Required (Android), Optional (iOS), string, PropertyName: ""</value>
        [JsonProperty(PropertyName = "title")]
        public string Title { get; set; }

        /// <summary>
        /// Indicates notification body text.
        /// Platform: Android, iOS
        /// </summary>
        /// <value>Optional, string, PropertyName: "body"</value>
        [JsonProperty(PropertyName = "body")]
        public string Body { get; set; }

        /// <summary>
        /// Indicates notification icon. On Android: sets value to myicon for drawable resource myicon.
        /// Platform: Android
        /// </summary>
        /// <value>Optional, string, PropertyName: "icon"</value>
        [JsonProperty(PropertyName = "icon")]
        public string Icon { get; set; }

        /// <summary>
        /// Indicates a sound to play when the device receives the notification. Supports default, or the filename of a sound resource bundled in the app.
        /// Android sound files must reside in /res/raw/, while iOS sound files can be in the main bundle of the client app or in the Library/Sounds folder of the app’s data container.
        /// Platform: Android, iOS
        /// </summary>
        /// <value>Optional, string, PropertyName: "sound"</value>
        [JsonProperty(PropertyName = "sound")]
        public string Sound { get; set; }

        /// <summary>
        /// Indicates the badge on client app home icon.
        /// Platform: iOS
        /// </summary>
        /// <value>Optional, string, PropertyName: "badge"</value>
        [JsonProperty(PropertyName = "badge")]
        public string Badge { get; set; }

        /// <summary>
        /// Indicates whether each notification message results in a new entry on the notification center on Android. If not set, each request creates a new notification. If set, and a notification with the same tag is already being shown, the new notification replaces the existing one in notification center.
        /// Platform: Android
        /// </summary>
        /// <value>Optional, string, PropertyName: "tag"</value>
        [JsonProperty(PropertyName = "tag")]
        public string Tag { get; set; }

        /// <summary>
        /// Indicates color of the icon, expressed in #rrggbb format
        /// Platform: Android
        /// </summary>
        /// <value>Optional, string, PropertyName: "color"</value>
        [JsonProperty(PropertyName = "color")]
        public string Color { get; set; }

        /// <summary>
        /// The action associated with a user click on the notification.
        /// On Android, if this is set, an activity with a matching intent filter is launched when user clicks the notification.
        /// If set on iOS, corresponds to category in APNS payload.
        /// Platform: Android, iOS
        /// </summary>
        /// <value>Optional, string, PropertyName: "click_action"</value>
        [JsonProperty(PropertyName = "click_action")]
        public string ClickAction { get; set; }

        /// <summary>
        /// Indicates the key to the body string for localization.
        /// On iOS, this corresponds to "loc-key" in APNS payload.
        /// On Android, use the key in the app's string resources when populating this value.
        /// Platform: Android, iOS
        /// </summary>
        /// <value>Optional, string, PropertyName: "body_loc_key"</value>
        [JsonProperty(PropertyName = "body_loc_key")]
        public string BodyLocKey { get; set; }

        /// <summary>
        /// Indicates the string value to replace format specifiers in body string for localization.
        /// On iOS, this corresponds to "loc-args" in APNS payload.
        /// On Android, these are the format arguments for the string resource. For more information, see Formatting strings.
        /// Platform: Android, iOS
        /// </summary>
        /// <see cref="http://developer.android.com/guide/topics/resources/string-resource.html#FormattingAndStyling"/>
        /// <value>Optional, JSON array as string, PropertyName: "body_loc_args"</value>
        [JsonProperty(PropertyName = "body_loc_args")]
        public ICollection<string> BodyLocArgs { get; set; }

        /// <summary>
        /// Indicates the key to the title string for localization.
        /// On iOS, this corresponds to "title-loc-key" in APNS payload.
        /// On Android, use the key in the app's string resources when populating this value.
        /// Platform: Android, iOS
        /// </summary>
        /// <value>Optional, string, PropertyName: "title_loc_key"</value>
        [JsonProperty(PropertyName = "title_loc_key")]
        public string TitleLocKey { get; set; }

        /// <summary>
        /// Indicates the string value to replace format specifiers in title string for localization.
        /// On iOS, this corresponds to "title-loc-args" in APNS payload.
        /// On Android, these are the format arguments for the string resource. For more information, see Formatting strings.
        /// Platform: Android, iOS
        /// </summary>
        /// <see cref="http://developer.android.com/guide/topics/resources/string-resource.html#FormattingAndStyling"/>
        /// <value>Optional, JSON array as string, PropertyName: "title_loc_args"</value>
        [JsonProperty(PropertyName = "title_loc_args")]
        public ICollection<string> TitleLocArgs { get; set; }
    }
}
