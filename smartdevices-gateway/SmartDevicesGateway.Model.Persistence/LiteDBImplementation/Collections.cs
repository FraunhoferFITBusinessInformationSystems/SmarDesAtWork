//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using LiteDB;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Persistence.Base;
using SmartDevicesGateway.Model.Persistence.Job;
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation.Types;
using SmartDevicesGateway.Model.Resource;
using SmartDevicesGateway.Model.Values;

namespace SmartDevicesGateway.Model.Persistence.LiteDBImplementation
{
    public class Collections
    {
        public LiteCollection<Jobs.Job> Job;
        public LiteCollection<DeviceId> DeviceID;
        public LiteCollection<DeviceInfo> DeviceInfo;
        public LiteCollection<BufferedJob> BufferedJob;
        public LiteCollection<DeviceIdBufferedJobLink> DeviceIdBufferedJobLink;
        public LiteCollection<DeviceIdDeviceInfoLink> DeviceIdDeviceInfoLink;
        public LiteCollection<ResourceInfo> ResourceInfo;
    }
}
