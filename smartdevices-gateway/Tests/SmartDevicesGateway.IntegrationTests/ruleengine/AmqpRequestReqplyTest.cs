//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Web;
using log4net;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Processing.Controller.Util;
using SmartDevicesGateway.TestCommon;
using SmartDevicesGateway.UnitTests;
using SmartDevicesGateway.UnitTests.Fixtures;
using Xunit;

namespace SmartDevicesGateway.IntegrationTests.ruleengine
{
    [Trait("Requires", Traits.BROKER)]
    [Trait("Category", Categories.INTEGRATION)]
    public class AmqpRequestReqplyTest : IClassFixture<AmqpFixture>
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        public const string Domain = "TodoSample";

        public AmqpFixture Fixture { get; }

        public AmqpRequestReqplyTest(AmqpFixture fixture)
        {
            Fixture = fixture;
            Fixture.LoggerFactory.CreateLogger<ConfigServiceTest>();
        }

        [Fact]
        [Trait("Requires", Traits.RULEENGINE)]
        public void TestTodoApi()
        {
            var msg = new
            {
                context = new { },
                domain = Domain
            };
            var response = Fixture.AmqpService.RequestReply(msg, "ToDoFind", "ToDo");
            Assert.NotNull(response);
        }

        [Fact]
        [Trait("Requires", Traits.RESOURCESERVICE)]
        public void TestResourceApi()
        {

            var path = "test/testjson.json";
            var response = Fixture.AmqpService.RequestReply(new { path }, "GetFile", "Resources");
            Assert.NotNull(response);
            Assert.NotNull(response["responseObject"]);
            Assert.NotNull(response["responseObject"]["body"]);
        }


        [Fact]
        [Trait("Requires", Traits.RESOURCESERVICE)]
        public void TestPutResourceLargePayload10K()
        {
            DynamicRequestLargePayload(10);
        }

        [Fact]
        [Trait("Requires", Traits.RESOURCESERVICE)]
        public void TestPutResourceLargePayload100K()
        {
            DynamicRequestLargePayload(100);
        }

        [Fact(Skip = "Error in AmqpDynamicRequest, needs fix!")]
        [Trait("Requires", Traits.RESOURCESERVICE)]
        public void TestPutResourceLargePayload1M()
        {
            DynamicRequestLargePayload(1024);
        }

        [Fact(Skip = "Error in AmqpDynamicRequest, needs fix!")]
        [Trait("Requires", Traits.RESOURCESERVICE)]
        public void TestPutResourceLargePayload20M()
        {
            DynamicRequestLargePayload(1024 * 20);
        }

        [Fact]
        [Trait("Tag", Tags.LONG_RUNNING)]
        public void TestPutResourceLargePayloadMocked10MB()
        {
            DynamicRequestLargePayloadWithMock(10);
        }

        [Fact(Skip = "Too Large Message for MockService.")]
        [Trait("Tag", Tags.LONG_RUNNING)]
        public void TestPutResourceLargePayloadMocked50MB()
        {
            DynamicRequestLargePayloadWithMock(50);
        }

        [Fact(Skip = "Too Large Message for MockService.")]
        [Trait("Tag", Tags.LONG_RUNNING)]
        public void TestPutResourceLargePayloadMocked100MB()
        {
            DynamicRequestLargePayloadWithMock(100);
        }

        private void DynamicRequestLargePayloadWithMock(int sizeMB)
        {
            var service = new ServiceMock(Fixture.AmqpService, "testRestourceserver", "ResourcesMock");
            DynamicRequestLargePayload(sizeMB*1024, "ResourcesMock", TimeSpan.FromSeconds(30), true);
        }

        private void DynamicRequestLargePayload(int sizeKB, string requestQueue = "Resources", TimeSpan? timeout = null, bool checkHashInPayload = false)
        {
            //generate large payload
            var rand = new Random(System.DateTimeOffset.Now.Millisecond);
            var ms = new MemoryStream();
            var buffer = new byte[1024];
            for (var written = 0L; written < sizeKB; written++)
            {
                rand.NextBytes(buffer);
                ms.Write(buffer);
            }
            var base64String = Convert.ToBase64String(ms.ToArray());

            var msg = new
            {
                name = $"rand{sizeKB}KB.bin",
                mimeType = "application/octet-stream",
                body = base64String
            };

            var response = Fixture.AmqpService.RequestReply(msg, "PutResource", requestQueue, timeout);
            Assert.NotNull(response);
            var amqpApiResponse = AmqpApiUtils.ParseApiResult(response);
            Assert.NotNull(amqpApiResponse);
            Assert.NotNull(amqpApiResponse.ResponseObject);
            Assert.NotNull(amqpApiResponse.ResponseObject["uuid"]);
            var guid = Guid.Parse(amqpApiResponse.ResponseObject["uuid"].ToObject<string>());

            if (checkHashInPayload)
            {
                Assert.NotNull(amqpApiResponse.ResponseObject["hash"]);
                var hash = amqpApiResponse.ResponseObject["hash"];
                var referenceHash = ServiceMock.Hash64Str(base64String);
                Assert.Equal(referenceHash, hash);
            }
        }
    }
}
