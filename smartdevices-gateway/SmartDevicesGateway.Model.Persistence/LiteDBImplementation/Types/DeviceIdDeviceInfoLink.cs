//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;

namespace SmartDevicesGateway.Model.Persistence.LiteDBImplementation.Types
{
    public class DeviceIdDeviceInfoLink
    {
        public int Id { get; set; }
        public DeviceId DeviceId { get; set; }
        public DeviceInfo DeviceInfo { get; set; }
    }
}
