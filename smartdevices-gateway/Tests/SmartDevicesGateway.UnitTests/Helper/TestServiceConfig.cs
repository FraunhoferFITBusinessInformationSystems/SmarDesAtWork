//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Common.Interfaces;

namespace SmartDevicesGateway.UnitTests
{
    public class TestServiceConfig : IConfig
    {
        public TestServiceAConfig TestServiceAConfig { get; set; }
        public TestServiceBConfig TestServiceBConfig { get; set; }
        public TestServiceCConfig TestServiceCConfig { get; set; }
    }

    public class TestServiceAConfig
    {
        public string A { get; set; }
    }

    public class TestServiceBConfig
    {
        public string B { get; set; }
    }

    public class TestServiceCConfig
    {
        public string C { get; set; }
    }
}
