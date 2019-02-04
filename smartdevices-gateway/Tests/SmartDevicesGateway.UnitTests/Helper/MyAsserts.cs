//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Persistence.Job;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Xunit;
using static Xunit.Assert;

namespace SmartDevicesGateway.UnitTests.Helper
{
    public class MyAsserts
    {
        public static void Equal(DeviceId expected, DeviceId actual)
        {
            NotNull(expected);
            NotNull(actual);

            Assert.Equal(expected.DeviceName, actual.DeviceName);
            Assert.Equal(expected.FullId, actual.FullId);
            Assert.Equal(expected.User, actual.User);
        }

        public static void Equal(DeviceInfo expected, DeviceInfo actual)
        {
            NotNull(expected);
            NotNull(actual);

            Assert.Equal(expected.DeviceName, actual.DeviceName);
            Assert.Equal(expected.family, actual.family);
            Assert.Equal(expected.FcmToken, actual.FcmToken);
            Assert.Equal(expected.Id, actual.Id);
            Assert.Equal(expected.Type, actual.Type);
            Equal(expected.properties, actual.properties);
        }

        public static void Equal(BufferedJob expected, BufferedJob actual)
        {
            NotNull(expected);
            NotNull(actual);

            Assert.Equal(expected.CreateDate, actual.CreateDate);
            MyAsserts.Equal(expected.Header, expected.Header);
            Assert.Equal(expected.Id, actual.Id);
            MyAsserts.Equal(expected.ReplyDate, actual.ReplyDate);
            Assert.Equal(expected.Sent, actual.Sent);
            MyAsserts.Equal(expected.SentDate, actual.SentDate);
            MyAsserts.Equal((Job)expected.Job, (Job)actual.Job);
            MyAsserts.Equal(expected.DeviceId, expected.DeviceId);
        }

        public static void Equal(DateTime expected, DateTime actual)
        {

            Assert.Equal(expected.Year, actual.Year);
            Assert.Equal(expected.Month, actual.Month);
            Assert.Equal(expected.Day, actual.Day);
            Assert.Equal(expected.Hour, actual.Hour);
            Assert.Equal(expected.Minute, actual.Minute);
            Assert.Equal(expected.Second, actual.Second);
            Assert.Equal(expected.Millisecond % 1000, expected.Millisecond % 1000);
            Assert.Equal(expected.Kind, expected.Kind);
        }

        public static void Equal(DateTimeOffset expected, DateTimeOffset actual)
        {
            Assert.Equal(expected.Year, actual.Year);
            Assert.Equal(expected.Month, actual.Month);
            Assert.Equal(expected.Day, actual.Day);
            Assert.Equal(expected.Hour, actual.Hour);
            Assert.Equal(expected.Minute, actual.Minute);
            Assert.Equal(expected.Second, actual.Second);
            Assert.Equal(expected.Millisecond % 1000, expected.Millisecond % 1000);
            Assert.Equal(expected.Offset, expected.Offset);
        }

        public static void Equal(Job expected, Job actual)
        {
            NotNull(expected);
            NotNull(actual);

            MyAsserts.Equal(expected.AssignedAt, actual.AssignedAt);
            Assert.Equal(expected.AssignedTo, actual.AssignedTo);
            MyAsserts.Equal(expected.CreatedAt, actual.CreatedAt);
            Assert.Equal(expected.CreatedBy, actual.CreatedBy);
            Assert.Equal(expected.Id, actual.Id);
            Assert.Equal(expected.Immediate, actual.Immediate);
            Assert.Equal(expected.Name, actual.Name);
            Assert.Equal(expected.Priority, actual.Priority);
            Assert.Equal(expected.ReferenceId, actual.ReferenceId);
            MyAsserts.Equal(expected.Resource, actual.Resource);
            Assert.Equal(expected.Status, actual.Status);
            Assert.Equal(expected.Type, actual.Type);
        }

        public static void Equal<T1, T2>(Dictionary<T1, T2> expected, Dictionary<T1, T2> actual)
        {
            NotNull(expected);
            NotNull(actual);

            Assert.Equal(expected.Count(), actual.Count());
            foreach (var expectedKeyPair in expected)
            {
                Assert.Equal(actual[expectedKeyPair.Key], expectedKeyPair.Value);
            }
        }
    }
}
