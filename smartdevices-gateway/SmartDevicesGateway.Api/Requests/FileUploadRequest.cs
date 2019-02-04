//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;

namespace SmartDevicesGateway.Api.Requests
{
    public class FileUploadRequest
    {
        public Guid Id { get; set; }
        public IFormFile File { get; set; }
    }
}
