//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Persistence.Base;
using SmartDevicesGateway.Model.Persistence.InMemoryImplementation;
using SmartDevicesGateway.Model.Persistence.Job;
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation;
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation.Types;
using SmartDevicesGateway.Model.Values;

namespace SmartDevicesGateway.Model.Persistence
{
    public class LiteDBPersistenceProvider : LiteDBWrapper, IPersistenceProvider
    {
        //private static readonly object _lock = new Object();
        private HistoryBuffer<string, Value> _valueBuffer { get; } = new HistoryBuffer<string, Value>
        {
            DefaultMaxHistoryEntries = 200,
            DefaultMaxHistoryLivetime = TimeSpan.Zero
        };

        public LiteDBPersistenceProvider(Stream stream) : base(stream)
        {
        }

        public LiteDBPersistenceProvider(string databaseFileName = "SmartDevicesGateway.db") : base(databaseFileName)
        {
        }

        //implementation IPersistenceProvider..
        public void RemoveJob(DeviceId deviceId)
        {
            var deviceIdBufferedJobLink = GetDeviceIdBufferedJobLink(deviceId);
            Remove(new[] { deviceIdBufferedJobLink });
        }

        public bool RemoveJob(DeviceId deviceId, Guid id)
        {
            var old = GetDeviceIdBufferedJobLink(deviceId).BufferedJobs.FirstOrDefault(x => x.Id == id);
            this.Collections.Job.Delete(x => x.LiteDbId == old.Job.LiteDbId);
            return this.Collections.BufferedJob.Delete(x => x.LiteDbId == old.LiteDbId) > 0;
        }

        public void RemoveAllJobs()
        {
            //lock (_lock)
            {
                Collections.Job.Delete(x => x != null);
                Collections.BufferedJob.Delete(x => x != null);
                Collections.DeviceIdBufferedJobLink.Delete(x => x != null);
            }
        }

        public void AddJob(DeviceId deviceId, Jobs.Job job)
        {
            //lock (_lock)
            {
                var foundDeviceIdBufferedJobLinks = GetDeviceIdBufferedJobLinkOrNull(deviceId);

                DeviceIdBufferedJobLink deviceIdBufferedJobLink;

                var bufferedJobToAdd = new BufferedJob
                {
                    DeviceId = deviceId,
                    Id = job.Id,
                    Job = job,
                    CreateDate = DateTimeOffset.Now,
                };

                if (foundDeviceIdBufferedJobLinks != null)
                {
                    deviceIdBufferedJobLink = foundDeviceIdBufferedJobLinks;
                }
                else
                {
                    deviceIdBufferedJobLink = new DeviceIdBufferedJobLink
                    {
                        DeviceId = deviceId
                    };
                }

                //add deviceId if not existent
                AddOrUpdate(deviceIdBufferedJobLink.DeviceId);

                //add job if not existent
                Collections.Job.Insert(job);

                deviceIdBufferedJobLink.BufferedJobs.Add(bufferedJobToAdd);

                //add bufferedJobToAdd
                Collections.BufferedJob.Insert(bufferedJobToAdd);

                if (foundDeviceIdBufferedJobLinks != null)
                {
                    Collections.DeviceIdBufferedJobLink.Update(deviceIdBufferedJobLink);
                }
                else
                {
                    Collections.DeviceIdBufferedJobLink.Insert(deviceIdBufferedJobLink);
                }
            }
        }

        public IBufferedJob FindJobById(Guid guid)
        {
            return GetBufferedJob(guid);
        }

        public IDeviceJobs GetByDeviceId(DeviceId deviceId)
        {
            var bufferedJobs = BufferedJobBase.Find(x => x.DeviceId.FullId == deviceId.FullId);

            var deviceJob = new DeviceJobs();
            foreach (var bufferedJob in bufferedJobs)
            {
                deviceJob.Add(bufferedJob);
            }

            return deviceJob;
        }

        public virtual bool TryGetDeviceInfos(DeviceId deviceId, out DeviceInfo info)
        {
            var deviceIdDeviceInfoLinks = BaseDeviceIdDeviceInfoLink.Find(x => x.DeviceId.FullId == deviceId.FullId);

            try
            {
                info = deviceIdDeviceInfoLinks.Single().DeviceInfo;
            }
            catch (Exception)
            {
                info = null;
                return false;
            }

            return info != null;
        }

        public virtual void AddOrUpdateDeviceInfos(DeviceId deviceId, DeviceInfo deviceInfo)
        {
            //lock (_lock)
            {
                var foundDeviceIdDeviceInfoLink = BaseDeviceIdDeviceInfoLink.Find(x => x.DeviceId.FullId == deviceId.FullId);

                var deviceIdDeviceInfoLinks = foundDeviceIdDeviceInfoLink.ToList();

                deviceInfo.Id = deviceId.FullId;

                if (deviceIdDeviceInfoLinks.Any())
                {
                    var link = deviceIdDeviceInfoLinks.Single(x => x.DeviceId.FullId == deviceId.FullId);
                    link.DeviceInfo = deviceInfo;
                    AddOrUpdate(deviceInfo);
                    Collections.DeviceIdDeviceInfoLink.Update(link);
                }
                else
                {
                    var deviceIdDeviceInfoLinkToAdd = new DeviceIdDeviceInfoLink { DeviceId = deviceId, DeviceInfo = deviceInfo };
                    AddOrUpdate(deviceId);

                    AddOrUpdate(deviceInfo);
                    Collections.DeviceIdDeviceInfoLink.Insert(deviceIdDeviceInfoLinkToAdd);
                    
                }
            }
        }

        public void AddValue(string key, Value value)
        {
            _valueBuffer.Add(key, value);
        }

        public Value GetValue(string key)
        {
            return _valueBuffer.Get(key);
        }

        public void UpdateJobStatus(Guid refJobId, JobStatus status)
        {
            var jobs = GetAllJobs()?.Where(x => x.Id == refJobId);

            foreach (var job in jobs)
            {
                job.Status = status;
                UpdateJob(job);
            }
        }

        public IEnumerable<DeviceId> FindDevicesByRefId(Guid jobReferenceId)
        {
            return Collections
                .BufferedJob.Find(x => x.Job.ReferenceId == jobReferenceId)
                .Select(x => x.DeviceId);
        }

        protected void UpdateJob(Jobs.Job job)
        {
            //lock (_lock)
            {
                Collections.Job.Update(job);
            }
        }

        public new IEnumerable<Jobs.Job> GetAllJobs()
        {
            return base.GetAllJobs();
        }

        public new IEnumerable<DeviceId> GetAllDeviceIds()
        {
            return base.GetAllDeviceIds();
        }
        
        public new IEnumerable<DeviceInfo> GetAllDeviceInfos()
        {
            return base.GetAllDeviceInfos();
        }
    }
}