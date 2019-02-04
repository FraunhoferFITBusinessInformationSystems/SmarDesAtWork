//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common.Interfaces;

namespace SmartDevicesGateway.Services.ConfigService.Providers
{
    public abstract class AbstractConfigProvider<T> : IConfigProvider<T> where T : class, IConfig
    {
        private T _config;
        private DateTimeOffset _lastUpdate = DateTimeOffset.MinValue;

        protected ILogger Logger { get; }

        public T Config
        {
            get => _config;
            protected set
            {
                var old = _config;
                _config = value;
                _lastUpdate = DateTimeOffset.Now;

                //Notify Change
                Logger.LogDebug("Notify ConfigChange listeners...");
                RaiseConfigChangeEvent(
                    new ConfigChangedEventArgs<T>
                    {
                        NewValue = _config,
                        OldValue = old
                    });
            }
        }

        public DateTimeOffset LastUpdate => _lastUpdate;

        protected AbstractConfigProvider(ILoggerFactory loggerFactory)
        {
            Logger = loggerFactory.CreateLogger<AbstractConfigProvider<T>>();
        }

        public T Get()
        {
            return Config;
        }

        protected virtual void RaiseConfigChangeEvent(ConfigChangedEventArgs<T> e)
        {
            ConfigChangedEvent?.Invoke(this, e);
        }

        public event EventHandler<ConfigChangedEventArgs<T>> ConfigChangedEvent;
    }
}
