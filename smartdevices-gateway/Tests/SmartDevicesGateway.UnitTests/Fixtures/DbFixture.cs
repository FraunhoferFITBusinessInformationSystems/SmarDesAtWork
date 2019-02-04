//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.UnitTests.Helper;

namespace SmartDevicesGateway.UnitTests.Fixtures
{
    public class DbFixture : ConfigFixture
    {
        public IPersistenceProvider PersistenceProvider { get; }

        private readonly MemoryStream _memoryStream;

        public DbFixture()
        {
            _memoryStream = new MemoryStream();
            PersistenceProvider = new LiteDBPersistenceProvider(_memoryStream);
        }

        public override void Dispose()
        {
            if (_memoryStream != null && _memoryStream.CanSeek)
            {
                _memoryStream.Close();
            }
            _memoryStream?.Close();
            base.Dispose();
        }
    }
}
