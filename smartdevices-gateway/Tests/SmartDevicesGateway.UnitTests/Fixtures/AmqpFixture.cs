//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using Amqp;
using Amqp.Framing;
using Amqp.Sasl;
using Amqp.Types;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Configuration.Memory;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json.Linq;
using SmartDevicesGateway.Common;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Services;
using SmartDevicesGateway.Services.ConfigService;
using Vogler.Amqp;

namespace SmartDevicesGateway.UnitTests.Fixtures
{
    public class AmqpFixture : ConfigFixture
    {
        public AmqpServiceMock AmqpService { get; }

        public AmqpFixture()
        {
            var conf = new AmqpServiceConfig()
            {
                ConnectionString = "amqp://user:password@localhost:5672",
                ClientName = "SmartDevicesGateway"
            };

            AmqpService = new AmqpServiceMock(conf);
        }
    }

    public class AmqpServiceMock : AmqpService
    {
        private Connection testConnection;
        private readonly AmqpServiceConfig amqpConfig;

        public AmqpServiceMock(AmqpServiceConfig amqpConfig) : base(amqpConfig)
        {
            this.amqpConfig = amqpConfig;
        }

        public Session GetSession()
        {
            return base.GetSession();
        }
    }
}
