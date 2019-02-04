//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Persistence.Job;
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation;
using System.Collections.Generic;

namespace SmartDevicesGateway.UnitTests.Helper
{
    public class LiteDBBaseMock : LiteDBWrapper
    {
        public LiteDBBaseMock(string databaseFileName = "SmartDevicesGateway.db") : base(databaseFileName)
        {

        }

        public void SaveDeviceId(DeviceId deviceId)
        {
            Collections.DeviceID.Insert(deviceId);
        }

        public IEnumerable<DeviceId> LoadDeviceId(string user, string deviceName)
        {
            return Collections.DeviceID.Find(x => x.DeviceName == deviceName && x.User == user);
        }

        public void SaveBufferedJob(BufferedJob BufferedJob)
        {
            Collections.DeviceID.Insert(BufferedJob.DeviceId);
            Collections.Job.Insert(BufferedJob.Job);
            Collections.BufferedJob.Insert(BufferedJob);
        }

        public IEnumerable<BufferedJob> LoadAllBufferedJobs()
        {
            return Collections.BufferedJob
                .Include(x => x.Job)
                .Include(x => x.DeviceId)
                .FindAll();
        }

        public new Collections Collections => base.Collections;


    }
}
