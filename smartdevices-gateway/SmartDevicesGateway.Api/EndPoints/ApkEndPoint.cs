//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.ComponentModel.DataAnnotations;
using System.IO;
using System.Linq;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Api.EndPoints.Base;
using SmartDevicesGateway.Processing.Controller.SmartDevice;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api/apk")]
    public class ApkEndPoint : AbstractEndpoint
    {
        private readonly ApkModel _resourceModel;

        public ApkEndPoint(ILoggerFactory loggerFactory, ApkModel resourceModel) : base(loggerFactory)
        {
            _resourceModel = resourceModel;
        }
        
        private const string ApkContentType = "application/vnd.android.package-archive";

        [HttpGet]
        public IActionResult GetInfo()
        {
            var apkVersions = _resourceModel.GetApkFiles().ToList();
            var phoneLatest = _resourceModel.GetApkPhoneLatest();
            var watchLatest = _resourceModel.GetApkWatchLatest();

            return Json(new
            {
                Latest = new
                {
                    Phone = phoneLatest?.ToString(),
                    Watch = watchLatest?.ToString()
                },
                All = apkVersions.Select(x => x.ToString()),
            });
        }

        [HttpGet("phone")]
        public IActionResult GetPhoneLatest()
        {
            var apkVersion = _resourceModel.GetApkPhoneLatest();
            var dir = _resourceModel.GetApkDirectory();

            if (dir == null || apkVersion == null)
            {
                return FormattedInternalServerError("Error in apk directory");
            }

            var filename = Path.Combine(dir, apkVersion.ToString());

            return CreateFileStreamResultForApk(filename);
        }

        [HttpGet("watch")]
        public IActionResult GetWatchLatest()
        {
            var apkVersion = _resourceModel.GetApkWatchLatest();
            var dir = _resourceModel.GetApkDirectory();

            if (dir == null || apkVersion == null)
            {
                return FormattedInternalServerError("Error in apk directory");
            }

            var filename = Path.Combine(dir, apkVersion.ToString());

            return CreateFileStreamResultForApk(filename);
        }

        [HttpGet("{name}")]
        public IActionResult GetSpecificVersion(
            [FromRoute, Required] string name)
        {

            var apkVersion = _resourceModel
                .GetApkFiles()
                .FirstOrDefault(x => x.ToString().Equals(name));
            var dir = _resourceModel.GetApkDirectory();

            if (dir == null || apkVersion == null)
            {
                return FormattedNotFound("Unknown Version");
            }

            var filename = Path.Combine(dir, apkVersion.ToString());

            return CreateFileStreamResultForApk(filename);
        }

        [Obsolete("This endpoint is obsolete and should no longer used.")]
        [HttpGet("/api/res/apk/latest")]
        public IActionResult GetLatest()
        {
            var apkVersion = _resourceModel.GetApkPhoneLatest();
            var dir = _resourceModel.GetApkDirectory();

            if (dir == null || apkVersion == null)
            {
                return FormattedInternalServerError("Error in apk directory");
            }

            var filename = Path.Combine(dir, apkVersion.ToString());

            return CreateFileStreamResultForApk(filename);
        }

        private FileStreamResult CreateFileStreamResultForApk(string apkFile)
        {
            if (!System.IO.File.Exists(apkFile))
            {
                throw new FileNotFoundException("Unknown APK-File");
            }

            var stream = new FileStream(apkFile, FileMode.Open, FileAccess.Read);
            var filename = Path.GetFileName(apkFile);
            Response.Headers.Add("Content-Disposition", $"inline; filename=\"{filename}\"");
            Response.Headers.Add("Content-Length", stream.Length.ToString());
            return new FileStreamResult(stream, ApkContentType);
        }
    }
}