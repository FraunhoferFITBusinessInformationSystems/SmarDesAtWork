//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;

namespace SmartDevicesGateway.Model.Persistence.Base
{
    public interface IStorageBuffer<in TId, T>
    {
        void Add(TId id, T obj);
        bool Has(TId id);
        T Get(TId id);
        bool Remove(TId id);
        void Remove(params TId[] ids);
        IEnumerable<T> GetAll();
    }
}