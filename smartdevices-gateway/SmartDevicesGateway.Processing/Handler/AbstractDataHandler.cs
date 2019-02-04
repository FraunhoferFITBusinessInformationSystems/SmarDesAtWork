//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using System.Linq;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Jobs;

namespace SmartDevicesGateway.Processing.Handler
{
    public abstract class AbstractDataHandler<T>
    {
        public ILogger Logger { get; }

        protected AbstractDataHandler(ILoggerFactory loggerFactory)
        {
            Logger = loggerFactory.CreateLogger<AbstractDataHandler<T>>();
        }

        public virtual IEnumerable<T> GetData(DeviceId deviceId)
        {
            return null;
        }
    }
}
