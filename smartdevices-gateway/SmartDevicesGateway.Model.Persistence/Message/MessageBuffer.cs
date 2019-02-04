//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Messages.Base;
using SmartDevicesGateway.Model.Persistence.Base;

namespace SmartDevicesGateway.Model.Persistence.Message
{
    public class MessageBuffer
    {
        protected Dictionary<DeviceId, DeviceMessages> Entries { get; }

        public MessageBuffer()
        {
            Entries = new Dictionary<DeviceId, DeviceMessages>();
        }

        public void Add(DeviceId id, IMessage obj)
        {
            GetEntry(id).Add(new BufferedMessage
            {
                DeviceId = id,
                Message = obj,
                CreateDate = DateTimeOffset.Now,
            });
        }

        public bool Has(DeviceId id)
        {
            return Entries.ContainsKey(id);
        }

        public IDeviceMessages Get(DeviceId id)
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

        public IEnumerable<IDeviceMessages> GetAll()
        {
            return Entries.Values.ToList();
        }

        protected DeviceMessages GetEntry(DeviceId deviceId)
        {
            if (!Entries.ContainsKey(deviceId))
            {
                Entries[deviceId] = new DeviceMessages();
            }
            return Entries[deviceId];
        }
    }
}
