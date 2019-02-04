//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common.Extensions;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Dto.Data;
using SmartDevicesGateway.Model.Extensions;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Requests;
using SmartDevicesGateway.Model.Ui;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Processing.Controller.Util;
using SmartDevicesGateway.Processing.Exceptions;
using SmartDevicesGateway.Processing.Handler;
using StringExtensions = SmartDevicesGateway.Common.Extensions.StringExtensions;

namespace SmartDevicesGateway.Processing.Controller.SmartDevice
{
    public class DataModel : AbstractModel
    {
        private readonly MessageHandler _messageHandler;
        private readonly ValueHandler _valueHandler;
        private readonly ConfigHandler _configHandler;

        public DataModel(ILoggerFactory loggerFactory,
            MessageHandler messageHandler, ValueHandler valueHandler, ConfigHandler configHandler) : base(loggerFactory)
        {
            _messageHandler = messageHandler;
            _valueHandler = valueHandler;
            _configHandler = configHandler;
        }

        public IEnumerable<ValueDto> GetValueData(DeviceId deviceId, int pageNumber, int pageSize, string filterBy,
            string filterString, bool filterExcluding,
            string filterStateBy, bool filterStateExcluding,
            string sortBy, bool sortAscending)
        {
            var config = _configHandler.GetDeviceConfig(deviceId);
            var valueSpecs = _configHandler.GetValueSpecifications(config.Values);
            var valueDto = valueSpecs.Select(x => new ValueDto
            {
                ConfigKey = x.Name,
                Name = x.DataSource.NameId,
                Value = _valueHandler.GetValue(x.DataSource.NameId)
            }).ToList();

            return valueDto;
        }

        public object GetAllTabs(DeviceId deviceId)
        {
            return new
            {
                jobs = GetTab(deviceId, "dashboard", new PaginationRequest(), new SortRequest(),
                    new List<AdvancedFilterRequest>()),
                actions = GetTab(deviceId, "actions", new PaginationRequest(), new SortRequest(),
                    new List<AdvancedFilterRequest>()),
                values = GetTab(deviceId, "livedata", new PaginationRequest(), new SortRequest(),
                    new List<AdvancedFilterRequest>()),
            };
        }

        public IEnumerable<TabEntryDto> GetTab(DeviceId deviceId, string tab, PaginationRequest pagination,
            SortRequest sort, IEnumerable<AdvancedFilterRequest> filter)
        {
            var config = _configHandler.GetDeviceConfig(deviceId);

            switch (tab)
            {
                case "dashboard":
                    return GetJobsTab(config, pagination, sort, filter);
                case "actions":
                    return GetActionsTab(config, pagination, sort, filter);
                case "livedata":
                    return GetValuesTab(config, pagination, sort, filter);
                default:
                    throw new UnknownKeyException();
            }
        }

        private IEnumerable<TabEntryDto> GetJobsTab(DeviceConfig config, PaginationRequest pagination, SortRequest sort,
            IEnumerable<AdvancedFilterRequest> filterRequest)
        {
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
            if (config.DeviceId.DeviceName == "B")
            {
                pagination.PageSize = 10;
                jobs = jobs.Where(x => x.Status != JobStatus.Done);
            }
            //END FIXME

            List<Job> list;

            if (pagination.PageNumber > 0 && pagination.PageSize > 0)
            {
                list = jobs.ToPagedList(pagination.PageNumber, pagination.PageSize);
            }
            else
            {
                list = jobs.ToList();
            }
            //            return list.Select(x => new TabEntryDto { Entry = x });
            var actionDefs = _configHandler.GetActions(config.Actions);
            var actionEntries = actionDefs.Select(x => new TabEntryDto { ListUi = x })
                .Where(x => string.Equals(x.ListUi.Tab, "dashboard", StringComparison.Ordinal))
                .OrderBy(x => x.ListUi.Id).ToList();
            var jobEntries = list.Select(x => new TabEntryDto {Entry = x}).ToList();

            jobEntries.AddRange(actionEntries);
            return jobEntries;
        }

        private IEnumerable<TabEntryDto> GetActionsTab(DeviceConfig config, PaginationRequest pagination,
            SortRequest sort, IEnumerable<AdvancedFilterRequest> filter)
        {
            if (string.IsNullOrEmpty(sort.SortBy))
            {
                sort.SortBy = "name";
            }

            return GetEntries(config, pagination, sort, filter, "actions");
        }

        private IEnumerable<TabEntryDto> GetValuesTab(DeviceConfig config, PaginationRequest pagination,
            SortRequest sort, IEnumerable<AdvancedFilterRequest> filter)
        {
            return GetEntries(config, pagination, sort, filter, "livedata");
        }

        private IEnumerable<TabEntryDto> GetEntries(
            DeviceConfig config,
            PaginationRequest pagination,
            SortRequest sort,
            IEnumerable<AdvancedFilterRequest> filter,
            string tabSelector)
        {
            var actionDefs = _configHandler.GetActions(config.Actions).AsQueryable()
                .Where(x => string.Equals(x.Tab, tabSelector, StringComparison.Ordinal));
            var entries = actionDefs.Select(x => new TabEntryDto {ListUi = x});
            
            foreach (var f in filter)
            {
                TabEntryFilter.AddFilter(ref entries, f.FilterBy, f.FilterString, f.FilterExcluding);
            }

            TabEntryFilter.AddSort(ref entries, sort.SortBy, sort.SortOrderAscending);

            List<TabEntryDto> list;
            if (pagination.PageNumber > 0 && pagination.PageSize > 0)
            {
                list = entries.ToPagedList(pagination.PageNumber, pagination.PageSize);
            }
            else
            {
                list = entries.ToList();
            }
            return list;
        }
    }
}