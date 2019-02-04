//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Api.EndPoints.Base;
using SmartDevicesGateway.Api.Requests;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Dto.Data;
using SmartDevicesGateway.Model.Requests;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.Processing.Exceptions;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api/data")]
    public class DataEndPoint : AbstractEndpoint
    {
        private readonly DataModel _DataModel;

        public DataEndPoint(ILoggerFactory loggerFactory, DataModel dataModel) : base(loggerFactory)
        {
            _DataModel = dataModel;
        }

        [HttpGet("tab/{device}/{tab}")]
        public IActionResult GetTab(
            [FromRoute, Required] string device,
            [FromRoute, Required] string tab,
            [FromQuery] PaginationRequestDto paginationDto,
            [FromQuery] SortRequestDto sortDto,
            [FromQuery] AdvancedFilterRequestDto filterRequestDto)
        {
            try
            {
                var deviceId = new DeviceId(device);
                var filter = AdvancedFilterRequestDto.ParseFilterRequest(filterRequestDto);
                var pagination = PaginationRequestDto.MapPaginationRequest(paginationDto);
                var sort = SortRequestDto.MapSortRequest(sortDto);

                var tabEntries = _DataModel.GetTab(deviceId, tab, pagination, sort, filter);

                return Json(tabEntries);
            }
            catch (UnknownDeviceIdException e)
            {
                Logger.LogError("Exception while execution:", e);
                return FormattedNotFound($"Device id not found.");
            }
            catch (UnknownKeyException e)
            {
                Logger.LogError("Exception while execution:", e);
                return FormattedNotFound($"Key not found.");
            }
            catch (Exception e)
            {
                Logger.LogError("Exception while execution:", e);
                return FormattedInternalServerError($"{nameof(e)} {e.Message}");
            }
        }

        [HttpGet("tab/{device}")]
        public IActionResult GetAllTabs(
            [FromRoute, Required] string device)
        {
            try
            {
                var deviceId = new DeviceId(device);

                var tabEntries = _DataModel.GetAllTabs(deviceId);
                return Json(tabEntries);
            }
            catch (UnknownDeviceIdException e)
            {
                Logger.LogError("Exception while execution:", e);
                return FormattedNotFound($"Device id not found.");
            }
            catch (Exception e)
            {
                Logger.LogError("Exception while execution:", e);
                return FormattedInternalServerError($"{nameof(e)} {e.Message}");
            }
        }

        [HttpGet("{device}/values")]
        public IActionResult GetValues(
            [FromRoute, Required] string device,
            [FromQuery] PaginationRequestDto pagination,
            [FromQuery] FilterRequest filter,
            [FromQuery] SortRequestDto sort)
        {
            try
            {
                var deviceId = new DeviceId(device);
                var dataDto = _DataModel.GetValueData(deviceId, pagination.PageNumber, pagination.PageSize, 
                    filter.FilterBy, filter.FilterString, filter.FilterExcluding, 
                    filter.FilterStateBy, filter.FilterStateExcluding,
                    sort.SortBy, sort.SortOrderAscending);
                return Json(dataDto);
            }
            catch (UnknownDeviceIdException e)
            {
                Logger.LogError("Exception while execution:", e);
                return FormattedNotFound($"Device id not found.");
            }
            catch (Exception e)
            {
                Logger.LogError("Exception while execution:", e);
                return FormattedInternalServerError($"{nameof(e)} {e.Message}");
            }
        }
    }
}
