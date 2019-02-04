//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using System.Linq;
using SmartDevicesGateway.Model.Dto.Config;

namespace SmartDevicesGateway.Model.Persistence.Base
{
    public class DeviceBuffer<T> : IStorageBuffer<DeviceId, T>
    {
        protected Dictionary<DeviceId, T> Entries { get; }

        public DeviceBuffer()
        {
            Entries = new Dictionary<DeviceId, T>();
        }

        public void Add(DeviceId id, T obj)
        {
            Entries[id] = obj;
        }

        public bool Has(DeviceId id)
        {
            return Entries.ContainsKey(id);
        }

        public T Get(DeviceId id)
        {
            return Entries[id];
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

        public IEnumerable<T> GetAll()
        {
            return Entries.Values.ToList();
        }
    }
}
