//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.TestCommon;
using SmartDevicesGateway.UnitTests;
using SmartDevicesGateway.UnitTests.Fixtures;
using Xunit;

namespace SmartDevicesGateway.IntegrationTests.ruleengine
{
    [Trait("Requires", Traits.BROKER)]
    [Trait("Category", Categories.INTEGRATION)]
    public class ResourceApiModelTest : IClassFixture<AmqpFixture>
    {
        public AmqpFixture Fixture { get; }

        public ResourceApiModelTest(AmqpFixture fixture)
        {
            Fixture = fixture;
            Fixture.LoggerFactory.CreateLogger<ConfigServiceTest>();
            Model = new ResourceModel(Fixture.LoggerFactory, Fixture.ConfigService, Fixture.AmqpService);
        }

        public ResourceModel Model { get; set; }

        public string Domain { get; set; } = "TodoSample";
        public string User { get; set; } = "debug1.A";

        [Fact]
        [Trait("Requires", "ruleengine")]
        [Trait("Requires", "resourceservice")]
        public async Task GetImageTest()
        {
            Uri uri = new Uri("smartdevices://resource/files/test/testimage.jpg");

            var path = "./test.jpg";

            var ret = await Model.GetResource(uri);
            using (var output = new FileStream(path, FileMode.Create, FileAccess.ReadWrite))
            {
                using (var stream = ret.Item2)
                {
                    CopyStream(stream, output);
                }
            }

            //validate file
            var fi = new FileInfo(path);
            Assert.Equal(ret.Item1.Size, fi.Length);
        }

        [Fact(Skip = "Deprecated, needs rework!")]
        public async Task GetSampleImageTest()
        {
            Uri uri = new Uri("smartdevices://resource/test/sampleimage.jpg");

            var path = "./test.jpg";

            var ret = await Model.GetResource(uri);
            using (var output = new FileStream(path, FileMode.Create, FileAccess.ReadWrite))
            {
                using (var stream = ret.Item2)
                {
                    CopyStream(stream, output);
                }
            }

            //validate file
            var fi = new FileInfo(path);
            Assert.Equal(ret.Item1.Size, fi.Length);
        }

        private static void CopyStream(Stream input, Stream output)
        {
            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = input.Read(buffer, 0, buffer.Length)) > 0)
            {
                output.Write(buffer, 0, len);
            }
        }
    }
}
