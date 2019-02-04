//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.ComponentModel.DataAnnotations;
using System.Runtime.InteropServices;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Api.EndPoints.Base;
using SmartDevicesGateway.Api.Requests;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.Processing.Exceptions;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api/jobs")]
    public class JobEndPoint : AbstractEndpoint
    {
        private readonly JobModel _jobModel;

        public JobEndPoint(ILoggerFactory loggerFactory, JobModel jobModel) : base(loggerFactory)
        {
            _jobModel = jobModel;
        }

        [HttpGet]
        public IActionResult GetAllJobs(
            [FromQuery] SortRequestDto sortDto,
            [FromQuery] AdvancedFilterRequestDto filterRequestDto)
        {
            try
            {
                var filter = AdvancedFilterRequestDto.ParseFilterRequest(filterRequestDto);
                var sort = SortRequestDto.MapSortRequest(sortDto);

                var jobEntries = _jobModel.GetAllJobs(sort, filter);

                return Json(jobEntries);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }

        [HttpGet("{device}")]
        public IActionResult GetJobs(
            [FromRoute, Required] string device,
            [FromQuery, Optional] PaginationRequestDto paginationDto,
            [FromQuery, Optional] SortRequestDto sortDto,
            [FromQuery, Optional] AdvancedFilterRequestDto filterRequestDto)
        {
            try
            {
                var deviceId = new DeviceId(device);
                var filter = AdvancedFilterRequestDto.ParseFilterRequest(filterRequestDto);
                var pagination = PaginationRequestDto.MapPaginationRequest(paginationDto);
                var sort = SortRequestDto.MapSortRequest(sortDto);

                var jobEntries = _jobModel.GetJobs(deviceId, pagination, sort, filter);

                return Json(jobEntries);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }

        [HttpGet("{device}/{id}")]
        public IActionResult GetJob(
            [FromRoute, Required] string device,
            [FromRoute, Required] Guid id)
        {
            try
            {
                var deviceId = new DeviceId(device);
                var jobEntries = _jobModel.GetJob(deviceId, id);

                return Json(jobEntries);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }

        [HttpDelete("{device}/{id}")]
        public IActionResult DeleteJob(
            [FromRoute, Required] string device,
            [FromRoute, Required] Guid id)
        {
            try
            {
                var deviceId = new DeviceId(device);
                var jobEntries = _jobModel.DeleteJob(deviceId, id);

                return Json(jobEntries);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }
    }
}
