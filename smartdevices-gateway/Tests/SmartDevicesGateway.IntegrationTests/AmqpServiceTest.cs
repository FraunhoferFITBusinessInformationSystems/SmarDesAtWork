//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Diagnostics;
using System.Security.Cryptography;
using System.Threading;
using System.Threading.Tasks;
using Amqp;
using Amqp.Framing;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Services.FcmService.Requests;
using SmartDevicesGateway.TestCommon;
using SmartDevicesGateway.UnitTests;
using SmartDevicesGateway.UnitTests.Fixtures;
using Vogler.Amqp;
using Xunit;
using Message = Amqp.Message;

namespace SmartDevicesGateway.IntegrationTests
{
    [Trait("Requires", Traits.BROKER)]
    [Trait("Category", Categories.INTEGRATION)]
    public class AmqpServiceTest : IClassFixture<ConfigFixture>
    {
        public ConfigFixture Fixture { get; }

        public AmqpServiceTest(ConfigFixture fixture)
        {
            Fixture = fixture;
            Fixture.LoggerFactory.CreateLogger<ConfigServiceTest>();
        }

        private static int ReceivedMessageNum { get; set; } = 0;

        [Fact]
        public void TestAmqpFixtureSendAndReceive()
        {
            var amqpFixture = new AmqpFixture();
            var service = amqpFixture.AmqpService;
            var num = new Random((int) DateTime.Now.Ticks).Next(999999);

            bool MessageReceiver(Message message)
            {
                var body = AmqpUtils.DeserializeMessage(message.Body);
                
                Assert.Equal("Test", body["Name"]);
                Assert.Equal(true, body["Test"]);

                ReceivedMessageNum++;
                return true;
            }

            var amqpListener = service.GetAmqpListener(MessageReceiver, $"TestQueue/{num}");

            Task.Delay(100);
            const int msgNum = 5;

            for (var i = 0; i < msgNum; i++)
            {
                var msg = new
                {
                    Name = "Test",
                    Test = true,
                    Value = i
                };

                service.SendMessage($"TestQueue/{num}", "Test", msg);
            }

            var watch = Stopwatch.StartNew();
            while (ReceivedMessageNum < msgNum && watch.ElapsedMilliseconds < 10000)
            {
                Thread.Sleep(100);
            }
            Assert.Equal(msgNum, ReceivedMessageNum);

            amqpListener.Disconnect();
        }

        [Fact]
        public void TestAmqpFixtureRequestReply()
        {
            var amqpFixture = new AmqpFixture();

            //Setup Listener:
            bool MessageReceiver(Message message)
            {
                var subject = message.Properties?.Subject;
                if (subject != null && subject == "EchoMessage")
                {
                    var m = AmqpUtils.DeserializeMessage(message.Body);
                    amqpFixture.AmqpService.SendMessage(message.Properties.ReplyTo, message.Properties.Subject, m, message.Properties.CorrelationId);
                    return true;
                }
                return false;
            }

            var listener = amqpFixture.AmqpService.GetAmqpListener(MessageReceiver, "TestRequest");

            //RequestReply Message:
            var requestMsg = new
            {
                Key = "Test"
            };
            var response = amqpFixture.AmqpService.RequestReply(requestMsg, "EchoMessage", "TestRequest");
            Assert.NotNull(response);
            Assert.NotNull(response["Key"]);
            Assert.Equal("Test", response["Key"]);

            listener.Disconnect();
        }
    }
}