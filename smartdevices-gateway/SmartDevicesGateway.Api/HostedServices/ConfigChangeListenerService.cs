//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Processing.Controller.Config;

namespace SmartDevicesGateway.Api.HostedServices
{
    public class ConfigChangeListenerService : BackgroundService
    {
        private readonly ConfigChangeModel _ValueModel;
        private ILogger<ConfigChangeListenerService> _logger;

        public ConfigChangeListenerService(ILoggerFactory loggerFactory, ConfigChangeModel valueModel)
        {
            _ValueModel = valueModel;
            _logger = loggerFactory.CreateLogger<ConfigChangeListenerService>();
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation($"ConfigChangeListenerService is starting.");

            stoppingToken.Register(() =>
                _logger.LogInformation($" ConfigChangeListenerService background task is stopping."));

            try
            {
                _ValueModel.StartListen();

                while (!stoppingToken.IsCancellationRequested)
                {
                    try
                    {
                        await Task.Delay(TimeSpan.FromSeconds(10), stoppingToken);
                    }
                    catch (TaskCanceledException)
                    {
                        _logger.LogDebug($"Listener interrupted");
                    }
                }

                _ValueModel.StopListen();

                _logger.LogInformation($"ConfigChangeListenerService background task is stopping.");
            }
            catch (Exception e)
            {
                _logger.LogError($"An Error occured during Background service execution", e);
            }
        }
    }
}