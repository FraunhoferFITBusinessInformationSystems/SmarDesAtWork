//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Persistence.Job;

namespace SmartDevicesGateway.Model.Persistence.InMemoryImplementation
{
    public class JobBuffer
    {
        protected Dictionary<DeviceId, DeviceJobs> Entries { get; }

        public JobBuffer()
        {
            Entries = new Dictionary<DeviceId, DeviceJobs>();
        }

        public void Add(DeviceId id, Jobs.Job obj)
        {
            GetEntry(id).Add(new BufferedJob
            {
                DeviceId = id,
                Id = obj.Id,
                Job = obj,
                CreateDate = DateTimeOffset.Now,
            });
        }

        public bool Has(DeviceId id)
        {
            return Entries.ContainsKey(id);
        }

        public IDeviceJobs GetByDeviceId(DeviceId id)
        {
            return GetEntry(id);
        }

        public bool Remove(DeviceId id)
        {
            return Entries.Remove(id);
        }

        public void Remove(params DeviceId[] ids)
        {
            foreach (var id in ids)
            {
                Remove(id);
            }
        }

        public void RemoveAll()
        {
            Entries.Clear();
        }

        public IEnumerable<IDeviceJobs> GetAll()
        {
            return Entries.Values.ToList();
        }

        protected DeviceJobs GetEntry(DeviceId deviceId)
        {
            if (!Entries.ContainsKey(deviceId))
            {
                Entries[deviceId] = new DeviceJobs();
            }
            return Entries[deviceId];
        }

        public IBufferedJob FindJobById(Guid referenceId)
        {
            var res =  Entries.Values
                .SelectMany(x => x.Jobs)
                .SingleOrDefault(x => x.Id == referenceId);

            return res;
        }
    }
}
