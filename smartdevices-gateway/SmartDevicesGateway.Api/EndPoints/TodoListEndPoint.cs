//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.IO;
using System.Linq.Expressions;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json.Linq;
using SmartDevicesGateway.Api.EndPoints.Base;
using SmartDevicesGateway.Api.Requests;
using SmartDevicesGateway.Common;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.Processing.Controller.Util;
using SmartDevicesGateway.Processing.Exceptions;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api/todo")]
    public class TodoListEndPoint : AbstractEndpoint
    {
        private readonly TodoListModel _toDoModel;

        public TodoListEndPoint(ILoggerFactory loggerFactory, TodoListModel toDoModel) : base(loggerFactory)
        {
            _toDoModel = toDoModel;
        }

   
        [HttpGet("lists")]
        public async Task<IActionResult> GetLists(
            [FromQuery] string context)
        {
            try
            {
                var todoListHeaders = await _toDoModel.GetLists(context);
                return Json(todoListHeaders);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }

        [HttpGet("lists/{key}")]
        public async Task<IActionResult> GetList(
            [FromRoute, Required] string key, 
            [FromQuery, Required] string context)
        {
            try
            {
                var todoListDetails = await _toDoModel.GetList(key, context);
                return Json(todoListDetails);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }

        [HttpGet("instances")]
        public async Task<IActionResult> GetInstances(
            [FromQuery, Required] string context)
        {
            try
            {
                var instances = await _toDoModel.GetInstances(context);
                return Json(instances);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }

        [JsonBodyPayload]
        [HttpPut("instances")]
        public async Task<IActionResult> StartInstance(
            [FromQuery, Required] string key,
            [FromQuery, Required] string contextDomain,
            [FromQuery, Required] string device,
            [FromBody] JObject context)
        {
            try
            {
                var instance = await _toDoModel.StartInstance(key, contextDomain, device, context);
                return Json(instance);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }

        [HttpGet("instances/{id}")]
        public async Task<IActionResult> GetInstance(
            [FromRoute, Required] Guid id)
        {
            try
            {
                var instance = await _toDoModel.GetInstance(id);
                return Json(instance);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }

        [HttpPut("instances/{id}/{step}")]
        [HttpPut("instances/{id}/{step}/{state}")]
        public async Task<IActionResult> ConfirmStep(
            [FromRoute, Required] Guid id,
            [FromRoute, Required] int step,
            [FromQuery, Required] string device,
            [FromRoute] bool state = true)
        {
            try
            {
                var instance = await _toDoModel.ConfirmStep(id, step, device, state);
                return Json(instance);
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }

        [HttpDelete("instances/{id}")]
        public async Task<IActionResult> CloseInstance(
            [FromRoute, Required] Guid id,
            [FromQuery, Required] string device)
        {
            try
            {
                var instance = await _toDoModel.CloseInstance(id, device);
                return Json(instance);
            }
            catch (AmqpApiResponseException e)
            {
                if (e.ResponseError.ErrorCode == "INSTANCE_ALREADY_CLOSED")
                {
                    return FormattedGone(e.Message);
                }

                return FormattedInternalServerError(
                    $"Error in API Respose: [{e.ResponseError?.Module}] {e.ResponseError?.ErrorCode} - {e.ResponseError.ErrorText}");
            }
            catch (Exception e)
            {
                return ResolveException(e);
            }
        }
    }
}
