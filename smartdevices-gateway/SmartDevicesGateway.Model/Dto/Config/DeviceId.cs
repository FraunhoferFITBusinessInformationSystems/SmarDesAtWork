//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.Model.Dto.Config
{
    public class DeviceId : IComparable<DeviceId>
    {
        public DeviceId()
        {
            //for litedb
        }

        public const string Separator = ".";

        public string User { get; set; }

        public string DeviceName { get; set; }

        public string FullId => User + Separator + DeviceName;

        public DeviceId(string user, string deviceName)
        {
            User = user;
            DeviceName = deviceName;
        }

        public DeviceId(string id)
        {
            var i = id.IndexOf(Separator, StringComparison.Ordinal);
            if (i < 0 || i == id.Length)
            {
                throw new FormatException("Cant read device ID");
            }

            User = id.Substring(0, i);
            DeviceName = id.Substring(i + 1);
        }

        public int CompareTo(DeviceId other)
        {
            return string.CompareOrdinal(FullId, other.FullId);
//
//
//            var i = string.Compare(User, other.User, StringComparison.Ordinal);
//            return i != 0 ? i : string.Compare(DeviceName, other.DeviceName, StringComparison.Ordinal);
        }

        public override bool Equals(object obj)
        {
            if (!(obj is DeviceId))
            {
                return false;
            }

            var other = (DeviceId) obj;

            return FullId.Equals(other.FullId);
        }


        public override int GetHashCode()
        {
            return Tuple.Create(User, DeviceName).GetHashCode();
        }

        public override string ToString()
        {
            return FullId;
        }

        public static DeviceId TryParse(string deviceId)
        {
            if (deviceId == null)
            {
                return null;
            }
            try
            {
                return new DeviceId(deviceId);
            }
            catch (FormatException)
            {
                return null;
            }
        }
    }
}
