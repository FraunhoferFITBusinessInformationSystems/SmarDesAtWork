//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text;

namespace SmartDevicesGateway.LoadTest
{
    interface IClient
    {
        void Initialize(Uri baseAddress, Tuple<string, string> args);

        void Start();

        void Stop();
    }
}
