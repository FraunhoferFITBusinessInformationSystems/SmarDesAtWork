//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace SmartDevicesGateway.Api.Requests
{
    public class FileUploadResponse
    {
        public bool Success { get; set; }

        public Guid Id { get; set; }

        public long Size { get; set; }

        [JsonProperty("Content-Type", NullValueHandling = NullValueHandling.Ignore)]
        public string ContentType { get; set; }
    }
}
