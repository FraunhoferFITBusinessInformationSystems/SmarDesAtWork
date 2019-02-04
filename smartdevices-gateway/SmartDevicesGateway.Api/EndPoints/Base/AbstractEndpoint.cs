//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;
using SmartDevicesGateway.Common;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.Processing.Exceptions;

namespace SmartDevicesGateway.Api.EndPoints.Base
{
    public class AbstractEndpoint : Controller
    {
        public ILogger Logger { get; set; }

        public AbstractEndpoint(ILoggerFactory loggerFactory)
        {
            Logger = loggerFactory.CreateLogger<AbstractEndpoint>();
        }

        protected void SetCacheControlHeadersToNoCache()
        {
            Response.Headers.Add(new KeyValuePair<string, StringValues>("Cache-Control", "no-cache"));
            Response.Headers.Add(new KeyValuePair<string, StringValues>("Pragma", "no cache"));
            Response.Headers.Add(new KeyValuePair<string, StringValues>("Expires", "0"));
        }

        protected void RemoveCacheControlHeaders()
        {
            if (Response.Headers.ContainsKey("Cache-Control"))
            {
                Response.Headers.Remove("Cache-Control");
            }

            if (Response.Headers.ContainsKey("Pragma"))
            {
                Response.Headers.Remove("Pragma");
            }

            if (Response.Headers.ContainsKey("Expires"))
            {
                Response.Headers.Remove("Expires");
            }
        }

        // 200
        protected JsonResult FormattedOk(string resultMessage = "OK", bool setCacheControlHeadersToNoCache = true)
        {
            Response.StatusCode = StatusCodes.Status200OK;

            // Cache-Control
            if (setCacheControlHeadersToNoCache)
            {
                SetCacheControlHeadersToNoCache();
            }
            else
            {
                RemoveCacheControlHeaders();
            }

            return Json(new
            {
                Result = resultMessage,
                Error = ""
            });
        }


        // 201
        protected JsonResult FormattedCreated<T>(T obj = null) where T : class
        {
            if (obj == null)
            {
                return FormattedCreated("Created");
            }

            Response.StatusCode = StatusCodes.Status201Created;
            RemoveCacheControlHeaders();
            return Json(new
            {
                Result = obj,
                Error = ""
            });
        }

        protected JsonResult FormattedCreated()
        {
            return FormattedCreated("Created");
        }


        // 304
        protected IActionResult FormattedNotModified()
        {
            Response.StatusCode = StatusCodes.Status304NotModified;
            RemoveCacheControlHeaders();

            return new EmptyResult();
        }


        // 400
        protected JsonResult FormattedBadRequest(string errorMessage = "Bad Request")
        {
            Response.StatusCode = StatusCodes.Status400BadRequest;
            SetCacheControlHeadersToNoCache();
            return Json(new
            {
                Result = "",
                Error = errorMessage
            });
        }


        // 401
        protected JsonResult FormattedUnauthorized(string errorMessage = "Unauthorized")
        {
            Response.StatusCode = StatusCodes.Status401Unauthorized;
            RemoveCacheControlHeaders();
            return Json(new
            {
                Result = "",
                Error = errorMessage
            });
        }


        // 404
        protected JsonResult FormattedNotFound(string errorMessage = "Not Found")
        {
            Response.StatusCode = StatusCodes.Status404NotFound;
            SetCacheControlHeadersToNoCache();
            return Json(new
            {
                Result = "",
                Error = errorMessage
            });
        }
        
        // 404
        protected JsonResult FormattedGone(string errorMessage = "Gone")
        {
            Response.StatusCode = StatusCodes.Status410Gone;
            SetCacheControlHeadersToNoCache();
            return Json(new
            {
                Result = "",
                Error = errorMessage
            });
        }

        // 415
        protected JsonResult FormattedUnsupportedMediaType(string errorMessage = "Unsupported Media Type")
        {
            Response.StatusCode = StatusCodes.Status415UnsupportedMediaType;
            SetCacheControlHeadersToNoCache();
            return Json(new
            {
                Result = "",
                Error = errorMessage
            });
        }

        // 422
        protected JsonResult FormattedUnprocessableEntity(string errorMessage = "Unprocessable Entity")
        {
            Response.StatusCode = StatusCodes.Status422UnprocessableEntity;
            SetCacheControlHeadersToNoCache();
            return Json(new
            {
                Result = "",
                Error = errorMessage
            });
        }


        // 500
        protected JsonResult FormattedInternalServerError(string errorMessage = "Internal Server Error")
        {
            Response.StatusCode = StatusCodes.Status500InternalServerError;
            RemoveCacheControlHeaders();
            return Json(new
            {
                Result = "",
                Error = errorMessage
            });
        }


        // 503
        protected JsonResult FormattedServiceUnavailable(string errorMessage = "Service Unavailable")
        {
            Response.StatusCode = StatusCodes.Status503ServiceUnavailable;
            RemoveCacheControlHeaders();
            return Json(new
            {
                Result = "",
                Error = errorMessage
            });
        }

        // 504
        protected JsonResult FormattedGatewayTimeout(string errorMessage = "Gateway Timeout")
        {
            Response.StatusCode = StatusCodes.Status504GatewayTimeout;
            RemoveCacheControlHeaders();
            return Json(new
            {
                Result = "",
                Error = errorMessage
            });
        }

        protected IActionResult ResolveException(Exception e)
        {
            Logger.LogError(e, "Exception while execution:");
            switch (e)
            {
                case UnknownDeviceIdException _:
                    return FormattedNotFound("Device id not found.");
                case UnknownKeyException _:
                    return FormattedNotFound($"Key not found: {e.Message}");
                case BadRequestException _:
                    return FormattedBadRequest(e.Message);
                case UnauthorizedException _:
                    return FormattedUnauthorized(e.Message);
                case NotFoundException _:
                    return FormattedNotFound(e.Message);
                case GoneException _:
                    return FormattedGone(e.Message);
                case InternalServerErrorException _:
                    return FormattedInternalServerError(e.Message);
                case GatewayTimeoutException _:
                    return FormattedGatewayTimeout(e.Message);
                case ServiceUnavailableException _:
                    return FormattedServiceUnavailable(e.Message);
                case UnsupportedMediaTypeException _:
                    return FormattedUnsupportedMediaType(e.Message);
                case UnprocessableEntityException _:
                    return FormattedUnprocessableEntity(e.Message);
            }
            return FormattedInternalServerError($"{e.GetType()}: {e.Message}");
        }
    }
}