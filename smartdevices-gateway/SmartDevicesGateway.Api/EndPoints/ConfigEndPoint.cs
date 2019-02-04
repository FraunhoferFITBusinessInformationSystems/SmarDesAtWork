//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using SmartDevicesGateway.Api.EndPoints.Base;
using SmartDevicesGateway.Model.Dto;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.Processing.Exceptions;
using SmartDevicesGateway.Services.FcmService;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api/config")]
    public class ConfigEndPoint : AbstractEndpoint
    {
        private readonly ConfigModel _ConfigModel;

        public ConfigEndPoint(ILoggerFactory loggerFactory, ConfigModel configModel) : base(loggerFactory)
        {
            _ConfigModel = configModel;
        }

        [HttpGet("{device}")]
        public IActionResult GetDeviceConfig([FromRoute, Required]string device)
        {
            try
            {
                var deviceId = new DeviceId(device);
                var configDto = _ConfigModel.GetDeviceConfig(deviceId);

                return Json(configDto);
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

        [HttpGet("info/server")]
        public IActionResult GetServerInfo()
        {
            return Json(_ConfigModel.GetServerInfo());
        }

        [HttpGet("info/{device}")]
        public IActionResult GetDeviceInfo(
            [FromRoute, Required] string device)
        {
            try
            {
                var deviceId = new DeviceId(device);

                var deviceInfoDto = _ConfigModel.GetDeviceInfo(deviceId);

                return Json(deviceInfoDto);
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

        [HttpPut("info/{device}")]
        public IActionResult UpdateDeviceInfos(
            [FromRoute, Required] string device,
            [FromBody, Required] DeviceInfo infos)
        {
            try
            {
                var deviceId = new DeviceId(device);

                Logger.LogWarning("Put Token: " + infos.FcmToken + " for device " + deviceId);
                var deviceInfogDto = _ConfigModel.UpdateDeviceInfo(deviceId, infos);



                return Json(deviceInfogDto);
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
