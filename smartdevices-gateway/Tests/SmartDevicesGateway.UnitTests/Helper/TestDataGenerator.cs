//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Jobs;
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.UnitTests.Helper
{
    public class TestDataGenerator
    {
        public static DeviceId GetDeviceId(byte individualizer = 0)
        {
            return new DeviceId()
            {
                DeviceName = $"deviceName{individualizer}",
                User = $"user{individualizer}"
            };
        }

        public static DeviceInfo GetDeviceInfo(string user, byte individualizer = 0)
        {
            return new DeviceInfo()
            {
                DeviceName = $"deviceName{individualizer}",
                family = Model.Enums.DeviceFamily.Watch,
                FcmToken = "fcmToken",
                Id = $"deviceName{individualizer}.{user}",
                Type = Model.Enums.DeviceType.Android,
                properties = new Dictionary<string, object>()
                {
                    { "A", new SimpleObject() },
                    { "B", new SimpleObject() }
                }

            };
        }

        public static Job GetJob(byte individualizer = 0)
        {
            var testDateTime = DateTimeOffset.Now;
            var testGuid = new Guid(new byte[] { individualizer, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });

            return new Job()
            {
                AssignedAt = testDateTime,
                CreatedAt = testDateTime,
                AssignedTo = "trainee",
                CreatedBy = "boss",
                Id = testGuid,
                Immediate = true,
                Name = "jobkey",
                Priority = Model.Messages.Enums.MessagePriority.Normal,
                ReferenceId = testGuid,
                Status = Model.Jobs.Enums.JobStatus.InProgress,
                Type = "type",
                Resource = new Dictionary<string, string>(){
                    { "resourceKey1", "resourceValue1"},
                    { "resourceKey2", "resourceValue2"},
                    { "resourceKey3", "resourceValue3"}
                }
            };
    }
    }
}
