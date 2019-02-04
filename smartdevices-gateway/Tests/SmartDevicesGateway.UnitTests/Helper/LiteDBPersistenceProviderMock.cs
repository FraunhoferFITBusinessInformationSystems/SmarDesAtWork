//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using LiteDB;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation;
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation.Types;
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.UnitTests.Helper
{
    public class LiteDBPersistenceProviderMock : LiteDBPersistenceProvider
    {
        public LiteDBPersistenceProviderMock(string databaseFileName = "SmartDevicesGateway.db") : base(databaseFileName)
        {

        }

        public Collections GetCollections()
        {
            return Collections;
        }

        public LiteCollection<DeviceIdDeviceInfoLink> DeviceIdDeviceInfoLink => BaseDeviceIdDeviceInfoLink;
    }
}
