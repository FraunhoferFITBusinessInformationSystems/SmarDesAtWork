//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
namespace SmartDevicesGateway.Model.Persistence.LiteDBImplementation
{
    internal static class TableNames
    {
        public const string Job = "job";
        public const string DeviceID = "deviceId";
        public const string DeviceInfo = "deviceInfo";
        public const string BufferedJob = "bufferedJob";
        public const string HistoryBuffer = "historyBuffer";
        public const string DeviceIdBufferedJobLink = "deviceIdBufferedJobLink";
        public const string DeviceIdDeviceInfoLink = "deviceIdDeviceInfoLink";
        public const string ResourceInfos = "resourceInfos";
    }
}
