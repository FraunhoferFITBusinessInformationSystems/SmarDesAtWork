//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Newtonsoft.Json;
using SmartDevicesGateway.Common.Extensions;

namespace SmartDevicesGateway.Model.Ui
{
    public class UiComponent
    {
        public string Type { get; set; }
        public string Id { get; set; }
        public string  Name { get; set; }

        /// <summary>
        /// TabKey where this action should be displayed, default is "dashboard"
        /// </summary>
        public string Tab { get; set; }

        [JsonExtensionData]
        public Dictionary<string, object> AdditionalProperties { get; set; } = new Dictionary<string, object>();


        public UiComponent()
        {
            Tab = "dashboard";
        }

        public UiComponent(UiComponent other)
        {
            this.Type = other.Type;
            this.Id = other.Id;
            this.Name = other.Name;
            this.Tab = other.Tab;
            other.AdditionalProperties.ForEach(kvpair => this.AdditionalProperties.Add(kvpair.Key, kvpair.Value));
        }        
    }
}
