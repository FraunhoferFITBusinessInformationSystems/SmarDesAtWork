//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Net.Mime;
using Newtonsoft.Json;

namespace SmartDevicesGateway.Model.Resource
{
    public class ResourceInfo
    {
        public int LiteDbId { get; set; }
        public string Id { get; set; }
        public string FileEnding { get; set; }
        public Uri Uri { get; set; }
        public string ETag { get; set; }
        public long Size { get; set; }
        public ContentType ContentType { get; set; }
        public DateTimeOffset LastModified { get; set; }

        public string RequestPath { get; set; }

        public string LocalFilename { get; set; }

        public ResourceInfo()
        {
        }
    }
}