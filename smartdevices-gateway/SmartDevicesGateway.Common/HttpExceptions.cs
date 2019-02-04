//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;

namespace SmartDevicesGateway.Common
{
    public abstract class HttpException : Exception
    {
        public int StatusCode { get; }
        
        protected HttpException(int statusCode, string message, Exception innerException) : base(message, innerException)
        {
            StatusCode = statusCode;
        }
    }

    public class BadRequestException : HttpException
    {
        public BadRequestException() : this("Bad Request")
        {
        }

        public BadRequestException(string message) : this(message, null)
        {
        }

        public BadRequestException(string message, Exception innerException) : base(400, message, innerException)
        {
        }
    }

    public class UnauthorizedException : HttpException
    {
        public UnauthorizedException() : this("Unauthorized")
        {
        }

        public UnauthorizedException(string message) : this(message, null)
        {
        }

        public UnauthorizedException(string message, Exception innerException) : base(401, message, innerException)
        {
        }
    }

    public class NotFoundException : HttpException
    {
        public NotFoundException() : this("Not Found")
        {
        }

        public NotFoundException(string message) : this(message, null)
        {
        }

        public NotFoundException(string message, Exception innerException) : base(404, message, innerException)
        {
        }
    }

    public class GoneException : HttpException
    {
        public GoneException() : this("Gone")
        {
        }

        public GoneException(string message) : this(message, null)
        {
        }

        public GoneException(string message, Exception innerException) : base(410, message, innerException)
        {
        }
    }

    public class UnsupportedMediaTypeException : HttpException
    {
        public UnsupportedMediaTypeException() : this("Unsupported Media Type")
        {
        }

        public UnsupportedMediaTypeException(string message) : this(message, null)
        {
        }

        public UnsupportedMediaTypeException(string message, Exception innerException) : base(415, message, innerException)
        {
        }
    }

    public class UnprocessableEntityException : HttpException
    {
        public UnprocessableEntityException() : this("Unprocessable Entity")
        {
        }

        public UnprocessableEntityException(string message) : this(message, null)
        {
        }

        public UnprocessableEntityException(string message, Exception innerException) : base(422, message, innerException)
        {
        }
    }

    public class InternalServerErrorException : HttpException
    {
        public InternalServerErrorException() : this("Internal Server Error")
        {
        }

        public InternalServerErrorException(string message) : this(message, null)
        {
        }

        public InternalServerErrorException(string message, Exception innerException) : base(500, message, innerException)
        {
        }
    }

    public class ServiceUnavailableException : HttpException
    {
        public ServiceUnavailableException() : this("Service Unavailable")
        {
        }

        public ServiceUnavailableException(string message) : this(message, null)
        {
        }

        public ServiceUnavailableException(string message, Exception innerException) : base(503, message, innerException)
        {
        }
    }

    public class GatewayTimeoutException : HttpException
    {
        public GatewayTimeoutException() : this("Gateway Timeout")
        {
        }

        public GatewayTimeoutException(string message) : this(message, null)
        {
        }

        public GatewayTimeoutException(string message, Exception innerException) : base(504, message, innerException)
        {
        }
    }
}
