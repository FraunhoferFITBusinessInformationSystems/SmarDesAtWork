//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Persistence.Base;
using SmartDevicesGateway.Model.Persistence.InMemoryImplementation;
using SmartDevicesGateway.Model.Persistence.Job;
using SmartDevicesGateway.Model.Persistence.Message;
using SmartDevicesGateway.Model.Resource;
using SmartDevicesGateway.Model.Values;
using SmartDevicesGateway.Services.ConfigService;

namespace SmartDevicesGateway.Model.Persistence
{
    public class InMemoryPersistenceProvider : IPersistenceProvider
    {
        private HistoryBuffer<string, Value> _valueBuffer { get; }
        private readonly Dictionary<DeviceId, DeviceInfo> _deviceInfos;
        private MessageBuffer _messageBuffer;
        private readonly JobBuffer _jobBuffer;

        public ILogger Logger { get; set; }

        public InMemoryPersistenceProvider(ILoggerFactory loggerFactory)
        {
            _valueBuffer = new HistoryBuffer<string, Value>
            {
                DefaultMaxHistoryEntries = 200,
                DefaultMaxHistoryLivetime = TimeSpan.Zero
            };

            _deviceInfos = new Dictionary<DeviceId, DeviceInfo>();
            _messageBuffer = new MessageBuffer();
            _jobBuffer = new JobBuffer();
            Logger = loggerFactory.CreateLogger<InMemoryPersistenceProvider>();


            // FIXME Persistence

            var devices = "devices";
            var dirname = Path.Combine(Directory.GetCurrentDirectory(), devices);

            if (Directory.Exists(dirname))
            {
                foreach (var fullFileName in Directory.GetFiles(dirname))
                {
                    var lines = File.ReadAllLines(fullFileName);

                    if (lines != null && lines.Length > 0)
                    {
                        var filename = Path.GetFileName(fullFileName);

                        var jsonString = string.Join("", lines);
                        var deviceInfo = JsonConvert.DeserializeObject<DeviceInfo>(jsonString, new JsonSerializerSettings
                        {
                            NullValueHandling = NullValueHandling.Ignore
                        });

                        _deviceInfos.Add(new DeviceId(filename), deviceInfo);
                    }
                }
            }
        }


        public void RemoveJob(DeviceId deviceId)
        {
            _jobBuffer.Remove(deviceId);
        }

        public bool RemoveJob(DeviceId deviceId, Guid id)
        {
            var byDeviceId = _jobBuffer.GetByDeviceId(deviceId);
            return byDeviceId.Remove(id);
        }

        public void RemoveAllJobs()
        {
            _jobBuffer.RemoveAll();
        }

        public void AddJob(DeviceId deviceId, Jobs.Job job)
        {
            _jobBuffer.Add(deviceId, job);
        }

        public IBufferedJob FindJobById(Guid guid)
        {
            return _jobBuffer.FindJobById(guid);
        }

        public IDeviceJobs GetByDeviceId(DeviceId deviceId)
        {
            return _jobBuffer.GetByDeviceId(deviceId);
        }

        public IEnumerable<Jobs.Job> GetAllJobs()
        {
            var bufferedJobs = _jobBuffer.GetAll()
                .Select(j => j.Jobs)
                .Aggregate(
                    new List<IBufferedJob>(),
                    (all, jobs) => {
                        all.AddRange(jobs);
                        return all;
                    });

            return bufferedJobs.Select(j => (Jobs.Job)j.Job).ToList();
        }

        public IEnumerable<DeviceId> GetAllDeviceIds()
        {
            return _deviceInfos.Keys;
        }

        public IEnumerable<DeviceInfo> GetAllDeviceInfos()
        {
            return _deviceInfos.Values;
        }

        public virtual bool TryGetDeviceInfos(DeviceId deviceId, out DeviceInfo deviceInfo)
        {
            try
            {
                var res = _deviceInfos.TryGetValue(deviceId, out deviceInfo);

                if (res && deviceInfo?.FcmToken != null)
                {
                    return true;
                }

                var devices = "devices";
                var dirname = Path.Combine(Directory.GetCurrentDirectory(), devices);
                var filename = Path.Combine(dirname, deviceId.FullId);
                var lines = File.ReadAllLines(filename);

                if (lines != null && lines.Length > 0)
                {
                    var jsonString = string.Join("", lines);
                    deviceInfo = JsonConvert.DeserializeObject<DeviceInfo>(jsonString, new JsonSerializerSettings
                    {
                        NullValueHandling = NullValueHandling.Ignore
                    });

                    return true;
                }

            }
            catch (Exception ex)
            {
                Logger.LogError(ex.Message);
            }

            deviceInfo = null;
            return false;
        }

        public virtual void AddOrUpdateDeviceInfos(DeviceId deviceId, DeviceInfo deviceInfo)
        {
            if (_deviceInfos.ContainsKey(deviceId))
            {
                _deviceInfos[deviceId] = deviceInfo;
            }
            else
            {
                _deviceInfos.Add(deviceId, deviceInfo);
            }

            try
            {
                var devices = "devices";
                var dirname = Path.Combine(Directory.GetCurrentDirectory(), devices);
                var filename = Path.Combine(dirname, deviceId.FullId);

                if (!Directory.Exists(dirname))
                {
                    Directory.CreateDirectory(dirname);
                }

                var json = JsonConvert.SerializeObject(deviceInfo, Formatting.Indented);
                File.WriteAllLines(filename, new[] { json });
            }
            catch (Exception ex)
            {
                Logger.LogError(ex.Message);
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
            var allJobs = GetAllJobs()
                .Where(j => j != null && j.Id.Equals(refJobId)).ToList();

            foreach (var job in allJobs)
            {
                job.Status = status;
            }
        }

        public IEnumerable<DeviceId> FindDevicesByRefId(Guid jobReferenceId)
        {
            throw new NotImplementedException();
        }

        public IEnumerable<ResourceInfo> GetResourceInfos()
        {
            throw new NotImplementedException();
        }

        public ResourceInfo GetResourceInfo(string path)
        {
            throw new NotImplementedException();
        }

        public void SetResourceInfo(ResourceInfo resourceInfo)
        {
            throw new NotImplementedException();
        }

        public bool RemoveResourceInfo(string path)
        {
            throw new NotImplementedException();
        }
    }
}