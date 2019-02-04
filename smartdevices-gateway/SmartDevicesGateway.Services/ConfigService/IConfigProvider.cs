//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Common.Interfaces;

namespace SmartDevicesGateway.Services.ConfigService
{
    public interface IConfigProvider<T> : IConfigProvider where T : IConfig
    {
        /// <summary>
        /// Provides the current configuration Object of the specified Type.
        /// </summary>
        /// <returns>Object of <see cref="T"/> or null</returns>
        T Get();

        /// <summary>
        /// Defines a EventHandler to listen for configuration changes of this
        /// specified configuration object.
        /// </summary>
        event EventHandler<ConfigChangedEventArgs<T>> ConfigChangedEvent;
    }

    public interface IConfigProvider
    {
    }
}
