//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using SmartDevicesGateway.Model.Persistence.Job;

namespace SmartDevicesGateway.Model.Persistence.Job
{
    public interface IDeviceJobs
    {
        IEnumerable<IBufferedJob> Jobs { get; }

        int Count { get; }

        int CountNotSent { get; }

        IBufferedJob GetJob(Guid jobId);

        bool HasJob(Guid jobId);

        bool Remove(Guid jobId);
    }
}