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
    /// <summary>
    /// Interface of the ConfigService to provide it through a dependency
    /// injection container. Provides methods to get all registered
    /// configurations as well as all respective configuration provider.
    /// </summary>
    public interface IConfigService
    {
        /// <summary>
        /// Provides the <see cref="IConfigProvider"/> instance of the stated
        /// <see cref="TConfig"/>-Type.
        /// </summary>
        /// <typeparam name="TConfig">Registered Config-Object</typeparam>
        /// <returns>The instance of the <see cref="IConfigProvider"/> or null
        /// if the specified config isn't registered.</returns>
        IConfigProvider<TConfig> GetProvider<TConfig>() where TConfig : class, IConfig;

        /// <summary>
        /// Gets the <see cref="TConfig"/>-Object from the <see cref="IConfigProvider"/>
        /// </summary>
        /// <typeparam name="TConfig">Registered Config-Object</typeparam>
        /// <returns>The <see cref="TConfig"/>-Object or null if there is no config present.</returns>
        TConfig Get<TConfig>() where TConfig : class, IConfig;

        /// <summary>
        /// Provides the ContentRoot-path of the currently running assembly
        /// </summary>
        string ContentRoot { get; }
    }
}
