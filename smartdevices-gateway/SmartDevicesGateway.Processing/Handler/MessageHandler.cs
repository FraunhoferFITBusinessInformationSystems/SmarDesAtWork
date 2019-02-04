//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using System.Linq;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Messages.Base;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Model.Persistence.Job;

namespace SmartDevicesGateway.Processing.Handler
{
    public class MessageHandler : AbstractDataHandler<IMessage>
    {
        private readonly IPersistenceProvider _persistenceProvider;

        public MessageHandler(ILoggerFactory loggerFactory, IPersistenceProvider persistenceProvider) : base(loggerFactory)
        {
            _persistenceProvider = persistenceProvider;
        }

        public IEnumerable<Job> GetJobs(DeviceConfig config)
        {
            var jobs = _persistenceProvider.GetByDeviceId(config.DeviceId).Jobs;

            return jobs.Select(x => x.Job).ToList();
        }

        public IEnumerable<IMessage> GetMessages(DeviceConfig config)
        {
            return new List<IMessage>();
        }

        public void CreateJob(string actionType, string actionName, string messageId)
        {
            throw new System.NotImplementedException();
        }
    }
}
