//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using System.Linq;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Persistence.InMemoryImplementation;

namespace SmartDevicesGateway.Processing.Controller.Base
{
    public class AbstractModel
    {
        public ILogger Logger { get; }

        protected AbstractModel(ILoggerFactory loggerFactory)
        {
            Logger = loggerFactory.CreateLogger<AbstractModel>();
        }
    }
}
