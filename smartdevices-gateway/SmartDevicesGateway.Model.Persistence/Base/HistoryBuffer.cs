//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;

namespace SmartDevicesGateway.Model.Persistence.Base
{
    public class HistoryBuffer<TId, T> : IStorageBuffer<TId, T>
    {
        private readonly Dictionary<TId, HistoricizedEntry<T>> _values = new Dictionary<TId, HistoricizedEntry<T>>();

        public int DefaultMaxHistoryEntries { get; set; } = 200;
        public TimeSpan DefaultMaxHistoryLivetime { get; set; } = TimeSpan.Zero;

        public void Add(TId id, T obj)
        {
            GetEntry(id).Value = obj;
        }

        public bool Has(TId id)
        {
            if (!_values.ContainsKey(id))
            {
                return false;
            }

            return (GetEntry(id).Count > 0);
        }

        public T Get(TId id)
        {
            return GetEntry(id).Value;
        }

        public bool Remove(TId id)
        {
            return _values.Remove(id);
        }

        public void Remove(params TId[] ids)
        {
            foreach (var id in ids)
            {
                Remove(id);
            }
        }

        public IEnumerable<T> GetAll()
        {
            return _values.Values.Select(x => x.Value).ToList();
        }

        public IEnumerable<ValueSnapshot<T>> GetHistory(TId id)
        {
            return GetEntry(id).ValueHistory;
        }

        private HistoricizedEntry<T> GetEntry(TId name)
        {
            if (!_values.ContainsKey(name))
            {
                _values[name] = new HistoricizedEntry<T>
                {
                    MaxHistoryLivetime = DefaultMaxHistoryLivetime,
                    MaxHistoryEntries = DefaultMaxHistoryEntries
                };
            }

            return _values[name];
        }
    }
}
