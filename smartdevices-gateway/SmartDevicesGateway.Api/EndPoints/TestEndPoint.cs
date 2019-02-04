//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Linq;
using System.Reflection.Metadata;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ApiExplorer;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Api.EndPoints.Base;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Notification;
using SmartDevicesGateway.Processing.Handler;
using Swashbuckle.AspNetCore.Swagger;
using Swashbuckle.AspNetCore.SwaggerGen;
using Operation = Microsoft.AspNetCore.JsonPatch.Operations.Operation;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api/test")]
    public class TestEndPoint : AbstractEndpoint
    {
        private readonly FcmMessageHandler _FcmMessageHandler;

        public TestEndPoint(ILoggerFactory loggerFactory, FcmMessageHandler fcmMessageHandler) : base(loggerFactory)
        {
            _FcmMessageHandler = fcmMessageHandler;
        }

        [HttpGet("sendfirebase/{msg}/{device}")]
        public IActionResult Sendfirebase(string msg, string device, [FromRoute] bool notify = true, [FromRoute] int pattern = 0)
        {
            try
            {
                var bdy = new FcmMessageBody
                {
                    Action = msg,
                    Notification = notify,
                    Pattern = pattern
                };
                var success = _FcmMessageHandler.SendFcmMessage(bdy, new DeviceId(device));
                return success ? FormattedOk() : FormattedInternalServerError();
            }
            catch (Exception ex)
            {
                Logger.LogError(ex.Message);
                return FormattedInternalServerError(ex.Message);
            }
        }

        /// <summary>
        /// Sends a Message via Firebase to the specified Device
        /// </summary>
        /// <param name="device">The device which should be notified. Format: device.id</param>
        /// <param name="msg">Desired Notification message. Choose out of 0=GetAll, 1=GetData, 2=GetConfig</param>
        /// <param name="notify">Choose if the Devices should Notify the user via Tone and Virbration</param>
        /// <param name="pattern">Set the Pattern type. 0 is Default. Valid patterns are 0 to 3</param>
        /// <returns></returns>
        [HttpGet("sendfirebase2/{msg}/{device}")]
        public IActionResult Sendfirebase2(string device, FirebaseActions msg, [FromQuery] bool notify = true, [FromQuery] int pattern = 0)
        {
            try
            {
                var bdy = new FcmMessageBody
                {
                    Action = msg.ToString(),
                    Notification = notify,
                    Pattern = pattern
                };
                var success = _FcmMessageHandler.SendFcmMessage(bdy, new DeviceId(device));
                return success ? FormattedOk() : FormattedInternalServerError();
            }
            catch (Exception ex)
            {
                Logger.LogError(ex.Message);
                return FormattedInternalServerError(ex.Message);
            }
        }

        public enum FirebaseActions
        {
            GetAll,
            GetData,
            GetConfig
        }

        [JsonBodyPayload]
        [HttpPost("rawjson")]
        public IActionResult TestRawJson()
        {
            try
            {
                return Ok();
            }
            catch (Exception ex)
            {
                Logger.LogError(ex.Message);
                return FormattedInternalServerError(ex.Message);
            }
        }
    }
}
