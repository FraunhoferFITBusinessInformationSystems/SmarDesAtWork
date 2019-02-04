//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common.Proxy;
using SmartDevicesGateway.Services;
using SmartDevicesGateway.Services.FcmService;
using SmartDevicesGateway.Services.FcmService.Requests;
using SmartDevicesGateway.Services.FcmService.Responses;
using SmartDevicesGateway.UnitTests;
using SmartDevicesGateway.UnitTests.Fixtures;
using Xunit;

namespace SmartDevicesGateway.IntegrationTests
{
    public class FcmServiceTest : IClassFixture<ConfigFixture>
    {
        public ConfigFixture Fixture { get; }

        public FcmServiceTest(ConfigFixture fixture)
        {
            Fixture = fixture;
            Fixture.LoggerFactory.CreateLogger<FcmServiceTest>();
        }

        [Fact]
        public void SendDryRunMessage()
        {
            var token = "your-token-here";

            var service = new FcmService(Fixture.ConfigService.Get<ServiceConfig>().FcmServiceConfig, ProxyConfig.DefaultConfig);
            var msg = new FcmMessageBuilder()
                .AddReceiver(token)
                .SetDebug()
                .SetData(new {Foo = "Bar"})
                .SetPriority(FcmMessagePriority.High)
                .SetNotification(new FcmNotification()
                {
                    Title = "Foobar",
                    Body = "Foo",
                    Sound = "default",
                    ClickAction = ""
                })
                .Build();
            
            var response = service.Send(msg);
            Assert.Equal(200, response.ErrorCode);
            Assert.NotNull(response.ResponseMessage);

//            Assert.Equal(1, response.ResponseMessage.Success);
//            Assert.Equal(0, response.ResponseMessage.Failure);
        }

        [Fact]
        public void SendDryRun200ErrorMessage()
        {
            //Send 200OK message but with internal failure
            var service = new FcmService(Fixture.ConfigService.Get<ServiceConfig>().FcmServiceConfig, ProxyConfig.DefaultConfig);
            var msg = new FcmMessageBuilder()
                .AddReceiver("", "some_sample_receiver_key")
                .SetDebug()
                .SetData(new { Foo = "Bar" })
                .Build();

            var response = service.Send(msg);
            Assert.Equal(200, response.ErrorCode);
            Assert.NotNull(response.ResponseMessage);

            Assert.Equal(0, response.ResponseMessage.Success);
            Assert.Equal(2, response.ResponseMessage.Failure);

            var errText = response.ResponseMessage.Results.First().Error;
            Assert.Equal(ResponseError.MissingRegistration, errText);

            errText = response.ResponseMessage.Results.Last().Error;
            Assert.Equal(ResponseError.InvalidRegistration, errText);
        }

        [Fact]
        public void SendDryRun400ErrorMessage()
        {
            //Send 400InvalidJson message
            var service = new FcmService(Fixture.ConfigService.Get<ServiceConfig>().FcmServiceConfig, ProxyConfig.DefaultConfig);
            Assert.Throws<FcmMessageBuilderException>(() =>
            {
                var msg = new FcmMessageBuilder()
                    .SetDebug()
                    .SetData(new {Foo = "Bar"})
                    .Build();
            });



//
//            var response = service.Send(msg);
//            Assert.Equal(400, response.ErrorCode);
//            Assert.NotNull(response.ErrorMessage);
        }
    }
}
