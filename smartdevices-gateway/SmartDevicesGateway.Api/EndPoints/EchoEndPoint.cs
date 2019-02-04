//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Api.EndPoints.Base;

namespace SmartDevicesGateway.Api.EndPoints
{
    [Route("api/echo")]
    public class EchoEndPoint : AbstractEndpoint
    {
        public EchoEndPoint(ILoggerFactory loggerFactory) : base(loggerFactory)
        {
        }

        [HttpGet]
        public IActionResult Get(string messageId, string reply)
        {
            try
            {
                return Json(DateTimeOffset.Now.ToString("yyyy-MM-dd'T'HH:mm:ss.fffK",
                    CultureInfo.InvariantCulture));
            }
            catch (Exception ex)
            {
                Logger.LogError(ex.Message);
                return FormattedInternalServerError();
            }
        }
    }
}
