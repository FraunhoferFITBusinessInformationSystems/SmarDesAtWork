//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.ComponentModel.DataAnnotations;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Mime;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using SmartDevicesGateway.Api.EndPoints.Base;
using SmartDevicesGateway.Model.Resource;
using SmartDevicesGateway.Processing.Controller.SmartDevice;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api")]
    public class ResourceEndPoint : AbstractEndpoint
    {
        private readonly ResourceModel _resourceModel;

        public ResourceEndPoint(ILoggerFactory loggerFactory, ResourceModel resourceModel) : base(loggerFactory)
        {
            _resourceModel = resourceModel;
        }

        // smardes://res/image/078a6-4386uf-68991c
        // http://server:7000/api/resources/images/todo/Aufr%C3%BCsten/step1.png
        // http://server:7000/api/resources/test/sampleimage.jpg
        // => smartdevices://resources/test/sampleimage.jpg

        
        [HttpGet("resources/{type}/{*path}")]
        public async Task<IActionResult> GetResource(
            [FromRoute, Required] string type,
            [FromRoute, Required] string path,
            [FromHeader(Name = "If-None-Match")] string etag)
        {
            if (string.IsNullOrEmpty(path) || string.IsNullOrEmpty(type))
            {
                Logger.LogTrace("Not Found: No Path Specified!");
                return FormattedNotFound("No Path specified!");
            }

            //Known Types:
            var knownTypes = new[] {"files", "images"};
            if (!knownTypes.Contains(type, StringComparer.CurrentCultureIgnoreCase))
            {
                Logger.LogTrace("Unknown Type specified!");
                return FormattedNotFound("Unknown Type specified!");
            }

            Uri uri = null;
            try
            {
                var uriStr = "smartdevices://resources/" + type + "/" + path;
                uri = new Uri(uriStr);
            }
            catch (UriFormatException e)
            {
                Logger.LogError(e, $"Bad Request: Could not parse Path: {type}/{path}");
                return FormattedBadRequest("Could not parse Path");
            }

            var ret = await _resourceModel.GetResource(uri);
            var resourceInfo = ret.Item1;
            var stream = ret.Item2;

            if (resourceInfo == null)
            {
                Logger.LogError("Not Found: Could not found requested Resource");
                return FormattedNotFound("Could not found requested Resource");
            }

            if (resourceInfo.ETag.Equals(etag))
            {
                return FormattedNotModified();
            }

            SetCacheControlHeadersToNoCache();
            Response.Headers.Add("Content-Disposition", $"inline; filename=\"{resourceInfo.Name}\"");
            Response.Headers.Add("Content-Type", resourceInfo.ContentType.ToString());
            Response.Headers.Add("Content-Length", resourceInfo.Size.ToString());
            Response.Headers.Add("ETag", resourceInfo.ETag);

            return new FileStreamResult(stream, resourceInfo.ContentType.ToString());
        }


        
        [HttpGet("images/{id}")]
        public async Task<IActionResult> GetImageResource(
            [FromRoute, Required] string id,
            [FromHeader(Name = "If-None-Match")] string etag)
        {
            if (string.IsNullOrEmpty(id) || !Guid.TryParse(id, out var guid))
            {
                Logger.LogTrace("Not Found: No Id Specified!");
                return FormattedNotFound("No Id specified!");
            }
            
            var ret = await _resourceModel.GetImageResource(guid);
            var resourceInfo = ret.Item1;
            var stream = ret.Item2;

            if (resourceInfo == null)
            {
                Logger.LogError("Not Found: Could not found requested Resource");
                return FormattedNotFound("Could not found requested Resource");
            }

            if (resourceInfo.ETag.Equals(etag))
            {
                return FormattedNotModified();
            }

            SetCacheControlHeadersToNoCache();
            Response.Headers.Add("Content-Disposition", $"inline; filename=\"{WebUtility.HtmlEncode(resourceInfo.Name)}\"");
            Response.Headers.Add("Content-Type", resourceInfo.ContentType.ToString());
            Response.Headers.Add("Content-Length", resourceInfo.Size.ToString());
            Response.Headers.Add("ETag", resourceInfo.ETag);

            return new FileStreamResult(stream, resourceInfo.ContentType.ToString());
        }

//        [RequestSizeLimit(100_000_000)]
        [HttpPost("images")]
        public async Task<IActionResult> PostImageResource(
            ResourceUploadRequest file)
        {
            if (file?.File == null)
            {
                return FormattedBadRequest("No data provided");
            }

            var info = new ResourceInfoDto()
            {
                Name = file.File.FileName,
                Size = file.File.Length,
            };

            try
            {
                info.ContentType = new ContentType(file.File.ContentType);
            }
            catch (Exception e)
            {
                Logger.LogError(e, "Unknown or no Content-Type specified!");
                return BadRequest("Unknown or no Content-Type specified!");
            }

            using (var readStream = file.File.OpenReadStream())
            {
                info.Uuid = await _resourceModel.PostImageResource(info, readStream);
            }

            SetCacheControlHeadersToNoCache();
            Response.Headers.Add("ETag", info.ETag);

            return Json(new ResourceUploadResponse()
            {
                Success = true,
                Id = info.Uuid.ToString(),
                Name = WebUtility.HtmlEncode(info.Name),
                ContentType = info.ContentType.ToString(),
                Size = info.Size,
                ETag = info.ETag,
                Url = "/api/images/" + info.Uuid.ToString()
            });
        }
    }

    public class ResourceUploadRequest
    {
        public Guid Id { get; set; }
        public IFormFile File { get; set; }
    }

    public class ResourceUploadResponse
    {
        public bool Success { get; set; }
        public string Id { get; set; }
        public long Size { get; set; }
        [JsonProperty("Content-Type", NullValueHandling = NullValueHandling.Ignore)]
        public string ContentType { get; set; }
        public string Name { get; set; }
        public string ETag { get; set; }
        public string Url { get; set; }
    }
}