//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.ComponentModel.DataAnnotations;
using System.Reflection;
using log4net;
using log4net.Repository.Hierarchy;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using SmartDevicesGateway.Api.EndPoints.Base;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Messages;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using ILoggerFactory = Microsoft.Extensions.Logging.ILoggerFactory;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api/actions")]
    public class ActionEndPoint : AbstractEndpoint
    {
        private readonly ActionModel _actionModel;

        private new static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        public ActionEndPoint(ILoggerFactory loggerFactory, ActionModel actionModel) : base(loggerFactory)
        {
            _actionModel = actionModel;
        }

        [HttpPut("jobs/{device}")]
        public IActionResult PutJob(
            [FromRoute] [Required] string device,
            [FromBody] [Required] Job job)
        {
            try
            {
                if (job == null)
                {
                    return BadRequest("Body empty");
                }

                var deviceId = new DeviceId(device);
                Logger.DebugLogMessage($"Received job from \"{deviceId}\":", job);
                _actionModel.PutJob(deviceId, job);

                return FormattedOk();
            }
            catch (FormatException e)
            {
                Logger.Error(e);
                return BadRequest("DeviceId fehlerhaft.");
            }
            catch (Exception e)
            {
                Logger.Error(e);
                return FormattedInternalServerError();
            }
        }

        [HttpGet("jobs/{device}/clearjobs")]
        public IActionResult RemoveJobs(
            [FromRoute] [Required] string device)
        {
            try
            {
                _actionModel.RemoveJob(new DeviceId(device));

                return FormattedOk();
            }
            catch (FormatException e)
            {
                Logger.Error(e);
                return BadRequest("DeviceId fehlerhaft.");
            }
            catch (Exception e)
            {
                Logger.Error(e);
                return FormattedInternalServerError(e.ToString());
            }
        }

        [HttpDelete("jobs/clearalljobs")]
        public IActionResult RemoveAllJobs()
        {
            try
            {
                _actionModel.RemoveAllJobs();

                return FormattedOk();
            }
            catch (Exception e)
            {
                Logger.Error(e);
                return FormattedInternalServerError(e.ToString());
            }
        }

        [LogRequestBody]
        [HttpPut("messages/{device}")]
        public IActionResult PutMessageReply(
            [FromRoute] [Required] string device,
            [FromBody] [Required] MessageReply reply)
        {
            try
            {
                var deviceId = new DeviceId(device);

                if (reply == null)
                {
                    return BadRequest("No Body provided");
                }

                if (reply.Action != null)
                {
                    Logger.DebugLogMessage($"Received MessageReply from \"{deviceId}\" with Action \"{reply.Action}\":", reply);
                    switch (reply.Action)
                    {
                        case "StartJob":
                            
                            _actionModel.StartNewJob(deviceId, reply);
                            break;
                        default:

                            break;
                    }
                }

                return FormattedOk();
            }
            catch (FormatException e)
            {
                Logger.Error(e);
                return BadRequest("DeviceId fehlerhaft.");
            }
            catch (Exception e)
            {
                Logger.Error(e);
                return FormattedInternalServerError();
            }
        }
    }

    public class LogRequestBodyAttribute : ActionFilterAttribute
    {
        //public override void OnActionExecuting(actionContext) {
        //    var x = "This is my custom line of code I need executed before any of the controller actions, for example log stuff";
        //    base.OnActionExecuting(actionContext);
        //}

        public override void OnActionExecuting(ActionExecutingContext context)
        {
            var v = context.ActionArguments.Keys;

            base.OnActionExecuting(context);
        }


    }
}