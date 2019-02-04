//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.IO;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common.Config;

namespace SmartDevicesGateway.UnitTests.Fixtures
{
    public class LoggingFixture : IDisposable
    {
        public ILoggerFactory LoggerFactory { get; set; }
        public string ContentRoot { get; set; }

        public LoggingFixture()
        {
            ContentRoot = ConfigurationPathUtils.ConfigurePathToContentRoot(false);
            LoggerFactory = new LoggerFactory().AddLog4Net(Path.Combine(ContentRoot, "config/log4net.config"));
        }

        public virtual void Dispose()
        {
            LoggerFactory?.Dispose();
        }
    }
}
