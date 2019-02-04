//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using SmartDevicesGateway.Model.Persistence.LiteDBImplementation;
using SmartDevicesGateway.UnitTests.Fixtures;
using System;
using System.IO;
using Xunit;

namespace SmartDevicesGateway.UnitTests.Helper
{
    public abstract class LiteDBTestBase : IDisposable
    {
        protected string DBName;
        protected LiteDBWrapper LiteDBWrapper;

        protected LiteDBTestBase()
        {
            
        }

        public void Dispose()
        {
            LiteDBWrapper?.Dispose();
            DeleteTestDB(DBName);
        }

        protected static void DeleteTestDB(string dbName)
        {
            if (File.Exists(dbName))
            {
                File.Delete(dbName);
            }
        }
    }
}
