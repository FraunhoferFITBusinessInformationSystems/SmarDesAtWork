//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;
using SmartDevicesGateway.Model.Amqp.Enums;

namespace SmartDevicesGateway.Model.Amqp
{
    public class AmqpResourceRequest
    {
        public ResourceAction Action { get; set; }

        public string Resource { get; set; }

        public string ResponsePath { get; set; }

        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public IList<AmqpFilterRequest> Filter { get; set; }

        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public IList<AmqpSortRequest> Sort { get; set; }

        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public AmqpPaginationRequest Pagination { get; set; }

        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public object Content { get; set; }
    }
}
