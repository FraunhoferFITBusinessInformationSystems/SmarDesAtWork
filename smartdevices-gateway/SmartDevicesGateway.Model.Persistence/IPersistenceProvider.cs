//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Persistence.Base;
using SmartDevicesGateway.Model.Persistence.Job;
using SmartDevicesGateway.Model.Persistence.Message;
using SmartDevicesGateway.Model.Resource;
using SmartDevicesGateway.Model.Values;
using SmartDevicesGateway.Services.FcmService;

namespace SmartDevicesGateway.Model.Persistence
{
    public interface IPersistenceProvider
    {
        void RemoveJob(DeviceId deviceId);

        bool RemoveJob(DeviceId deviceId, Guid id);
        void RemoveAllJobs();
        void AddJob(DeviceId deviceId, Jobs.Job job);
        IBufferedJob FindJobById(Guid guid);
        IDeviceJobs GetByDeviceId(DeviceId deviceId);

        IEnumerable<Jobs.Job> GetAllJobs();

        IEnumerable<DeviceId> GetAllDeviceIds();

        IEnumerable<DeviceInfo> GetAllDeviceInfos();

        bool TryGetDeviceInfos(DeviceId deviceId, out DeviceInfo info);
        void AddOrUpdateDeviceInfos(DeviceId deviceId, DeviceInfo deviceInfo);

        void AddValue(string key, Value value);
        Value GetValue(string key);
        void UpdateJobStatus(Guid refJobId, JobStatus status);
        IEnumerable<DeviceId> FindDevicesByRefId(Guid jobReferenceId);


        IEnumerable<ResourceInfo> GetResourceInfos();
        ResourceInfo GetResourceInfo(string path);
        void SetResourceInfo(ResourceInfo resourceInfo);
        bool RemoveResourceInfo(string path);
    }
}
