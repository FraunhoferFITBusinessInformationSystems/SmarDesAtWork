//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.TestCommon;
using SmartDevicesGateway.UnitTests.Fixtures;
using Xunit;

namespace SmartDevicesGateway.UnitTests
{
    [Trait("Category", Categories.UNIT)]
    public class ApkModelTest : IClassFixture<LoggingFixture>
    {
        public LoggingFixture Fixture { get; }
        private readonly ILogger _logger;

        public ApkModelTest(LoggingFixture fixture)
        {
            Fixture = fixture;
            Fixture.LoggerFactory.CreateLogger<ConfigServiceTest>();
        }

        [Fact]
        public void TestApkVersionParsing()
        {
            var strings = new string[] {
                "smartdevicesapp.app.watch.0.2.3-debug.apk",
                "smartdevicesapp.app.watch.0.2.1-debug.apk",
                "smartdevicesapp.app.phone.0.3.6-debug.apk",
                "smartdevice_sapp.app.phone.0.3.9-debug.apk"
            };

            foreach (var s in strings)
            {
                var p = ApkModel.ParseApkFilename(s);
                Assert.NotNull(p);
                Assert.Equal(s, p.ToString());
            }
        }

        [Fact]
        public void TestApkVersionCompare()
        {
            var strings = new string[] {
                "smartdevicesapp.app.watch.0.2.3-debug.apk",
                "smartdevicesapp.app.watch.0.2.3-debug.apk",
                "smartdevicesapp.app.watch.0.2.1-debug.apk",
                "smartdevicesapp.app.watch.0.2.2-debug.apk",
                "smartdevicesapp.app.phone.0.2.3-debug.apk"
            };

            var parsed = new ApkModel.ApkVersion[strings.Length];
            for (var i = 0; i < strings.Length; i++)
            {
                parsed[i] = ApkModel.ParseApkFilename(strings[i]);
                Assert.NotNull(parsed[i]);
            }

            //Equal
            Assert.True(parsed[0].CompareTo(parsed[0]) == 0);
            Assert.True(parsed[0].CompareTo(parsed[1]) == 0);
            Assert.True(parsed[1].CompareTo(parsed[0]) == 0);

            Assert.True(parsed[0].CompareTo(parsed[4]) != 0);
            
            //Different
            Assert.True(parsed[1].CompareTo(parsed[2]) > 0);
            Assert.True(parsed[2].CompareTo(parsed[1]) < 0);

            Assert.True(parsed[2].CompareTo(parsed[3]) < 0);
        }
    }
}
