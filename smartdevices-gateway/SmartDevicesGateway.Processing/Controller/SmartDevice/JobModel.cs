//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Dto.Data;
using SmartDevicesGateway.Model.Extensions;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Model.Requests;
using SmartDevicesGateway.Model.Ui;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Processing.Controller.Util;
using SmartDevicesGateway.Processing.Exceptions;
using SmartDevicesGateway.Processing.Handler;

namespace SmartDevicesGateway.Processing.Controller.SmartDevice
{
    public class JobModel : AbstractModel
    {
        private readonly MessageHandler _messageHandler;
        private readonly ConfigHandler _configHandler;
        private readonly IPersistenceProvider _persistenceProvider;

        public JobModel(ILoggerFactory loggerFactory,
            MessageHandler messageHandler, ConfigHandler configHandler, IPersistenceProvider persistenceProvider) : base(loggerFactory)
        {
            _messageHandler = messageHandler;
            _configHandler = configHandler;
            _persistenceProvider = persistenceProvider;
        }

        public Dictionary<DeviceId, IEnumerable<JobEntryDto>> GetAllJobs(SortRequest sort,
            IList<AdvancedFilterRequest> filterRequest)
        {
            var allDeviceIds = _persistenceProvider.GetAllDeviceIds().ToList();
            var data = new Dictionary<DeviceId, IEnumerable<JobEntryDto>>();
            foreach (var deviceId in allDeviceIds)
            {
                data[deviceId] = GetJobs(deviceId, null, sort, filterRequest);
            }
            return data;
        }

            public IEnumerable<JobEntryDto> GetJobs(DeviceId deviceId, PaginationRequest pagination, SortRequest sort,
            IEnumerable<AdvancedFilterRequest> filterRequest)
        {
            var config = _configHandler.GetDeviceConfig(deviceId);
            var jobs = _messageHandler.GetJobs(config)
                .AsQueryable()
                .Cast<Job>();

            foreach (var filter in filterRequest)
            {
                JobFilter.AddFilter(ref jobs, filter.FilterBy, filter.FilterString, filter.FilterExcluding);
            }
            JobFilter.AddJobSort(ref jobs, sort.SortBy, sort.SortOrderAscending);

            //FIXME -> Pagination in Watch implementieren !
            //Momentan für Watch -> limit 10 Jobs !!!
//            if (config.DeviceId.DeviceName == "B")
//            {
//                if (pagination == null)
//                {
//                    pagination = new PaginationRequest();
//                }
//                pagination.PageSize = 10;
//                jobs = jobs.Where(x => x.Status != JobStatus.Done);
//            }
            //END FIXME

            List<Job> list;
            if (pagination != null && pagination.PageNumber > 0 && pagination.PageSize > 0)
            {
                list = jobs.ToPagedList(pagination.PageNumber, pagination.PageSize);
            }
            else
            {
                list = jobs.ToList();
            }

            var uiLayouts = _configHandler.GetUiLayouts();

            return list.Select(x => new JobEntryDto()
            {
                Entry = x,
                Ui = uiLayouts.FirstOrDefault(y => y.Id == x.Name)
            });
        }

        public JobEntryDto GetJob(DeviceId deviceId, Guid id)
        {
            var config = _configHandler.GetDeviceConfig(deviceId);
            var query = _messageHandler.GetJobs(config)
                .AsQueryable()
                .Cast<Job>();

            query = query.Where(x => x.Id == id);
            var job = query.FirstOrDefault();

            if (job == null)
            {
                throw new UnknownKeyException("Unknown UUID");
            }
            var ui = _configHandler.GetUiLayouts()
                .FirstOrDefault(layout => string.Equals(layout.Id, job.Name, StringComparison.Ordinal));

            return new JobEntryDto()
            {
                Entry = job,
                Ui = ui
            };
        }

        public bool DeleteJob(DeviceId deviceId, Guid id)
        {
            var config = _configHandler.GetDeviceConfig(deviceId);
            var query = _messageHandler.GetJobs(config)
                .AsQueryable()
                .Cast<Job>();

            query = query.Where(x => x.Id == id);
            var job = query.FirstOrDefault();

            if(job == null)
            {
                throw new UnknownKeyException("Unknown JobID");
            }

            return _persistenceProvider.RemoveJob(deviceId, id);
        }
    }
}
