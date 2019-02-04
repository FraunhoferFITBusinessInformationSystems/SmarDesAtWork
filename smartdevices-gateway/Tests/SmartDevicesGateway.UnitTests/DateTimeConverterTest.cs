//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation;
using SmartDevicesGateway.UnitTests.Fixtures;
using System;
using SmartDevicesGateway.TestCommon;
using Xunit;

namespace SmartDevicesGateway.UnitTests
{
    [Trait("Category", Categories.UNIT)]
    public class DateTimeConverterTest : IClassFixture<LoggingFixture>
    {
        [Fact]
        public void DateTimeTest()
        {
            var dt1 = DateTimeOffset.Now;
            var dt1Str = DateTimeConverter.ToString(dt1);
            var dt2 = DateTimeConverter.ToDateTime(dt1Str);
            var dt2Str = DateTimeConverter.ToString(dt2);
            Assert.Equal(dt1, dt2);
            Assert.Equal(dt1Str, dt2Str);
        }

        [Fact]
        public void DateTimeOffsetTest()
        {
            var dt1 = DateTimeOffset.Now;
            var dt1Str = DateTimeConverter.ToString(dt1);
            var dt2 = DateTimeConverter.ToDateTimeOffset(dt1Str);
            var dt2Str = DateTimeConverter.ToString(dt2);
            Assert.Equal(dt1, dt2);
            Assert.Equal(dt1Str, dt2Str);
        }
    }
}
