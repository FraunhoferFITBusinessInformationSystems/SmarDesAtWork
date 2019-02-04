//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Common.Interfaces;
using SmartDevicesGateway.Services.AuthService;
using SmartDevicesGateway.Services.FcmService;
using Vogler.Amqp;

namespace SmartDevicesGateway.Services
{
    public class ServiceConfig : IConfig
    {
        public AmqpServiceConfig AmqpServiceConfig { get; set; }
        public AuthServiceConfig AuthServiceConfig { get; set; }
        public FcmServiceConfig FcmServiceConfig { get; set; }
    }
}
