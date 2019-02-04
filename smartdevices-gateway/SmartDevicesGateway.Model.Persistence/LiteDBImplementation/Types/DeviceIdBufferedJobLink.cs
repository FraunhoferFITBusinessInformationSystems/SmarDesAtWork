//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using LiteDB;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Persistence.Job;
using System.Collections.Generic;

namespace SmartDevicesGateway.Model.Persistence.LiteDBImplementation.Types
{
    public class DeviceIdBufferedJobLink
    {
        public int Id { get; private set; }
        public DeviceId DeviceId { get; set; }
        private List<BufferedJob> _bufferedJobs;
        public List<BufferedJob> BufferedJobs {
            get {
                if (_bufferedJobs == null) _bufferedJobs = new List<BufferedJob>();
                return _bufferedJobs;
            }
            private set {
                _bufferedJobs = value;
            }
        }
    }
}
