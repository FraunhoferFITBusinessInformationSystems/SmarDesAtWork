//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Todo;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.Services.ConfigService;

namespace SmartDevicesGateway.Processing.Handler
{
    public class TodoListHandler
    {
        private readonly ILogger _logger;
        private readonly IConfigService _configService;

        public TodoListHandler(ILoggerFactory loggerFactory, IConfigService configService)
        {
            _logger = loggerFactory.CreateLogger<ConfigHandler>();
            _configService = configService;
        }

        public IList<TodoListInstance> Instances { get; set; } = new List<TodoListInstance>();
    }
}
