//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace SmartDevicesGateway.Processing.Controller.Util
{
    public static class AmqpApiUtils
    {
        public static AmqpApiResponse ParseApiResult(JObject jObject)
        {
            if (jObject == null)
            {
                throw new AmqpApiException("No result provided or Amqp Timeout occurred!");
            }
            AmqpApiResponse response = null;
            try
            {
                response = jObject.ToObject<AmqpApiResponse>();
            }
            catch (Exception e)
            {
                throw new AmqpApiParseException("Could not parse Server Response.", e);
            }

            if (response.Error != null)
            {
                throw new AmqpApiResponseException(response.Error, "Error response from Server");
            }

            return response;
        }
    }

    public class AmqpApiResponse
    {
        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public JArray ResponseCollection { get; set; }

        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public JObject ResponseObject { get; set; }

        public AmqpApiResponseError Error { get; set; }
    }

    public class AmqpApiResponseError
    {
        public string Module { get; set; }
        public string ErrorCode { get; set; }
        public string ErrorText { get; set; }
    }

    public class AmqpApiException : Exception
    {
        public AmqpApiException()
        {
        }

        public AmqpApiException(string message) : base(message)
        {
        }

        public AmqpApiException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }

    public class AmqpApiResponseException : AmqpApiException
    {
        public AmqpApiResponseError ResponseError { get; }

        public AmqpApiResponseException()
        {
        }

        public AmqpApiResponseException(string message) : base(message)
        {
        }

        public AmqpApiResponseException(AmqpApiResponseError responseError, string message) : 
            base($"{message}: {responseError.Module} - Code: {responseError.ErrorCode} - {responseError.ErrorText}")
        {
            ResponseError = responseError;
        }

        public override string ToString()
        {
            return base.ToString();
        }
    }

    public class AmqpApiParseException : AmqpApiException
    {
        public AmqpApiParseException()
        {
        }

        public AmqpApiParseException(string message) : base(message)
        {
        }

        public AmqpApiParseException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
