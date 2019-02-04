//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.IO;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using SmartDevicesGateway.Api.EndPoints.Base;
using SmartDevicesGateway.Api.Requests;
using SmartDevicesGateway.Common;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api/media")]
    public class MediaEndPoint : AbstractEndpoint
    {
        private string _cacheDirectory;
        private string _imageCacheDirectory;

        public MediaEndPoint(ILoggerFactory loggerFactory) : base(loggerFactory)
        {
            var location = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            _cacheDirectory = Path.Combine(location, "cache");
            _imageCacheDirectory = Path.Combine(_cacheDirectory, "images");
            Directory.CreateDirectory(_imageCacheDirectory);
        }

        [HttpGet("images/{id}")]
        public IActionResult GetImage(
            [FromRoute, Required] Guid id)
        {
            var files = Directory.GetFiles(_imageCacheDirectory, id.ToString() + ".*");

            if (files.Length == 0)
            {
                return FormattedNotFound("Unknown id");
            }

            var file = files.First();
            var filename = Path.GetFileName(file);
            var ending = FileUtils.GetFileEnding(file);
            var contentType = FileUtils.GetContentTypeFromEnding(ending);

            var stream = new FileStream(file, FileMode.Open);

            Response.Headers.Add("Content-Disposition", $"inline; filename=\"{filename}\"");
            Response.Headers.Add("Content-Length", stream.Length.ToString());

            return new FileStreamResult(stream, contentType);
        }

        [HttpPost("images")]
        public async Task<IActionResult> PostImage(
            FileUploadRequest file)
        {
            if (file?.File == null)
            {
                return FormattedBadRequest("No data");
            }

            var id = file.Id;

            if (id.Equals(Guid.Empty))
            {
                id = Guid.NewGuid();
            }

//            var contentType = GetFileEnding(file.File.FileName);
//            if (contentType == null)
//                return FormattedBadRequest("Unknown File type");

            if (file.File.Length > 0)
            {
                var ending = FileUtils.GetEndingFromContentType(file.File.ContentType);

                var filePath = Path.Combine(_imageCacheDirectory, id.ToString() + "." + ending);

                using (var stream = new FileStream(filePath, FileMode.Create))
                {

                    await file.File.CopyToAsync(stream);
                }
            }

            return Json(new FileUploadResponse
            {
                ContentType = file.File.ContentType,
                Id = id,
                Success = true,
                Size = file.File.Length,
            });
        }
    }
}
