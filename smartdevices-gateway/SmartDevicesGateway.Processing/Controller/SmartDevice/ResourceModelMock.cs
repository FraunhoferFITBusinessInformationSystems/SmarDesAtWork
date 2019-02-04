//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.IO;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using log4net.Util;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Model.Resource;
using SmartDevicesGateway.Model.Values;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Processing.Handler;
using SmartDevicesGateway.Services.ConfigService;

namespace SmartDevicesGateway.Processing.Controller.SmartDevice
{

    public class ResourceModelMock : AbstractModel
    {
        private readonly IConfigService _configService;
        private readonly IPersistenceProvider _persistenceProvider;

        public string ResourceDirectory { get; private set; }

        public ResourceModelMock(ILoggerFactory loggerFactory, IConfigService configService, IPersistenceProvider persistenceProvider)  : base(loggerFactory)
        {
            _configService = configService;
            _persistenceProvider = persistenceProvider;
            SetResourceDirectory();
        }

        private void SetResourceDirectory()
        {
            var dirFromConfig = _configService.Get<AppConfig>().ResourceDirectory;
            var contentRoot = _configService.ContentRoot;

            if (!Path.IsPathRooted(dirFromConfig))
            {
                ResourceDirectory = Path.Combine(contentRoot, dirFromConfig);
            }
            else
            {
                ResourceDirectory = dirFromConfig;
            }
            Directory.CreateDirectory(ResourceDirectory);
        }

        public ResourceInfo GetResourceInfo(Uri uri)
        {
            var path = uri.Authority + uri.AbsolutePath;
            return _persistenceProvider.GetResourceInfo(path);
        }

        public Stream GetResourceStream(ResourceInfo resourceInfo)
        {
            if (ResourceDirectory == null || !Directory.Exists(ResourceDirectory))
            {
                throw new IOException("Unknown directory specified!");
            }

            var file = Path.Combine(ResourceDirectory, resourceInfo.LocalFilename);
            if (!File.Exists(file))
            {
                throw new Exception("File not found!");
            }
            var stream = new FileStream(file, FileMode.Open, FileAccess.Read);
            return stream;
        }

        public async Task<ResourceInfo> PostResource(ResourceInfo info, Stream uploadStream)
        {
            var path = info.RequestPath;
            var filename = PrettifyFilename(path) + info.FileEnding;
            var filepath = Path.Combine(ResourceDirectory, filename);

            using (var fileStream = new FileStream(filepath, FileMode.Create, FileAccess.Write))
            {
                await uploadStream.CopyToAsync(fileStream);
            }

            info.LocalFilename = filename;
            info.ETag = info.Id;
            info.LastModified = DateTimeOffset.Now;

            _persistenceProvider.SetResourceInfo(info);
            return info;
        }

        public string PrettifyFilename(string unpretty)
        {
            var pretty = "/\\?%*:|\"<>."
                .ToCharArray()
                .Aggregate(unpretty, 
                    (s, c) => s.Replace(c+"", ""));
            return pretty;
        }
    }
}
