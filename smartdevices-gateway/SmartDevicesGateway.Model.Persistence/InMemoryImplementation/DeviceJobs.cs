//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Model.Persistence.Job;
using System;
using System.Collections.Generic;
using System.Linq;

namespace SmartDevicesGateway.Model.Persistence.InMemoryImplementation
{
    public class DeviceJobs : IDeviceJobs
    {
        private readonly List<BufferedJob> _jobs = new List<BufferedJob>();
        public IEnumerable<IBufferedJob> Jobs => _jobs;

        public int Count => _jobs.Count;
        public int CountNotSent => _jobs.Count(x => !x.Sent);
        public IBufferedJob GetJob(Guid jobId)
        {
            return _jobs.Find((x => x.Id == jobId));
        }

        public bool HasJob(Guid jobId)
        {
            return _jobs.Any(x => x.Id == jobId);
        }

        public void Add(BufferedJob bufferedJob)
        {
            _jobs.Add(bufferedJob);
        }

        public bool Remove(Guid id)
        {
            var old = _jobs.FirstOrDefault(x => x.Id.Equals(id));
            return old != null && _jobs.Remove(old);
        }
    }
}