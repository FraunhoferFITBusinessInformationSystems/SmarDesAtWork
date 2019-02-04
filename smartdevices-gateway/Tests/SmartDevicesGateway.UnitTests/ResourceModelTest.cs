//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Mime;
using System.Text;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authentication.Internal;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Resource;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.TestCommon;
using SmartDevicesGateway.UnitTests.Fixtures;
using Xunit;

namespace SmartDevicesGateway.UnitTests
{
    [Trait("Category", Categories.UNIT)]
    public class ResourceModelTest : IClassFixture<DbFixture>, IDisposable
    {
        public DbFixture Fixture { get; }

        private readonly string _path;
        private readonly Uri _uri;
        private List<string> _disposeFiles = new List<string>();

        private readonly ILogger _logger;

        private ResourceModelMock _resourceModel;

        public ResourceModelTest(DbFixture fixture)
        {
            Fixture = fixture;
            _logger = Fixture.LoggerFactory.CreateLogger<ConfigServiceTest>();

            _resourceModel = new ResourceModelMock(
                Fixture.LoggerFactory, 
                Fixture.ConfigService,
                Fixture.PersistenceProvider);

            _path = "res.images/" + Guid.NewGuid();
            _uri = new Uri("smardes://" + _path);
        }

        [Fact]
        public async Task WriteResourceTest()
        {
            var sampleImage = Path.Combine(Fixture.ContentRoot, "TestResources", "sample-image.png");

            var info = new ResourceInfo()
            {
                ContentType = new ContentType("image/png"),
                FileEnding = ".png",
                Id = _uri.LocalPath.Replace("/", ""),
                RequestPath = _path,
                Uri = _uri,
                ETag = null
            };

            ResourceInfo newInfo = null;
            using (var stream = new FileStream(sampleImage, FileMode.Open, FileAccess.Read))
            {
                info.Size = stream.Length;
                newInfo = await _resourceModel.PostResource(info, stream);
            }
            _disposeFiles.Add(Path.Combine(_resourceModel.ResourceDirectory, newInfo.LocalFilename));
            
            Assert.NotNull(newInfo);
            Assert.NotNull(newInfo.ETag);
            Assert.NotNull(newInfo.LocalFilename);
        }

        [Fact]
        public async Task ReadResourceInfoTest()
        {
            //First: copy file:
            await WriteResourceTest();

            var info = _resourceModel.GetResourceInfo(_uri);
            Assert.NotNull(info);
            Assert.Equal(_uri.ToString(), info.Uri.ToString());
            Assert.False(string.IsNullOrEmpty(info.LocalFilename));
        }

        [Fact]
        public async Task ReadResourceDataTest()
        {
            //First: copy file:
            await WriteResourceTest();

            var info = _resourceModel.GetResourceInfo(_uri);
            Assert.NotNull(info);
            Assert.Equal(_uri.ToString(), info.Uri.ToString());
            Assert.False(string.IsNullOrEmpty(info.LocalFilename));

            using (var resourceStream = _resourceModel.GetResourceStream(info))
            {
                using (var outputStream = new MemoryStream())
                {
                    await resourceStream.CopyToAsync(outputStream);

                    outputStream.Seek(0, SeekOrigin.Begin);

                    var sampleImage = Path.Combine(Fixture.ContentRoot, "TestResources", "sample-image.png");
                    using (var compareStream = new FileStream(sampleImage, FileMode.Open, FileAccess.Read))
                    {
                        Assert.Equal(compareStream.Length, outputStream.Length);
                    }

                }
            }
            
        }

        public void Dispose()
        {
            foreach (var file in _disposeFiles)
            {
                try
                {
                    File.Delete(file);
                }
                catch (Exception)
                {
                    // ignored
                }
            }
            _disposeFiles.Clear();
        }
    }
}
