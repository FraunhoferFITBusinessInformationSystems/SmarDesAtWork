//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.IO;
using System.Net;
using System.Reflection;
using System.Text;
using log4net;
using Newtonsoft.Json;
using SmartDevicesGateway.Common.Proxy;
using SmartDevicesGateway.Services.ConfigService;
using SmartDevicesGateway.Services.FcmService.Requests;
using SmartDevicesGateway.Services.FcmService.Responses;

namespace SmartDevicesGateway.Services.FcmService
{
    public class FcmService : IFcmService
    {
        private readonly ProxyConfig _proxySettings;
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        public FcmServiceConfig Config { get; }
        private readonly JsonSerializerSettings _jsonSettings;

        public FcmService(FcmServiceConfig configService, ProxyConfig proxySettings)
        {
            _proxySettings = proxySettings;
            Config = configService;
            _jsonSettings = new JsonSerializerSettings
            {
                Formatting = Formatting.Indented,
                NullValueHandling = NullValueHandling.Ignore
            };
        }

        public IFcmResponse Send(FcmMessage message)
        {
            ServicePointManager.ServerCertificateValidationCallback +=
                (sender, cert, chain, sslPolicyErrors) => true;

            var fcmResponse = new FcmResponse();

            var request = (HttpWebRequest)WebRequest.Create(Config.ApiUrl);
            request.Method = "POST";
            request.Headers.Add("Authorization", "key=" + Config.ServerKey);
            request.ContentType = "application/json";

            request.SetProxyFromConfig(_proxySettings);

            //serialize data
            var json = JsonConvert.SerializeObject(message, _jsonSettings);
            var byteArray = Encoding.UTF8.GetBytes(json);
            request.ContentLength = byteArray.Length;
            //write data to stream
            using (var dataStream = request.GetRequestStream())
            {
                dataStream.Write(byteArray, 0, byteArray.Length);
            }

            //log request
            var builder = new StringBuilder("Sending FCM Request to Firebase:\n------------------------------------\n");
            builder.Append($"Method: ....................: POST\n");
            builder.Append($"Authorization: .............: key={Config.ServerKey.Substring(0, Config.ServerKey.Length > 8 ? 8 : Config.ServerKey.Length)}... <omitted>\n");
            builder.Append($"ContentType: ...............: application/json\n");
            builder.Append($"Body: ......................: {json}\n");
            builder.Append("------------------------------------");
            Logger.Debug(builder.ToString());

            using (var response = (HttpWebResponse)request.GetResponseWithoutException())
            {
                //Evaluate Response
                var statusCode = (int)response.StatusCode;
                fcmResponse.ErrorCode = statusCode;

                if (statusCode >= 500 && statusCode <= 599)
                {
                    fcmResponse.ErrorMessage = ParseStringResponse(response);
                    fcmResponse.Error = ResponseError.InternalServerError;
                }
                else
                {
                    switch (statusCode)
                    {
                        case 401:
                            fcmResponse.ErrorMessage = ParseStringResponse(response);
                            fcmResponse.Error = ResponseError.AuthenticationError;
                            break;
                        case 400:
                            fcmResponse.ErrorMessage = ParseStringResponse(response);
                            fcmResponse.Error = ResponseError.InvalidJson;
                            break;
                        case 200:
                            fcmResponse.ResponseMessage = ParseMessageResponse(response);
                            fcmResponse.Error = ResponseError.NoError;
                            break;
                        default:
                            fcmResponse.ResponseMessage = ParseMessageResponse(response);
                            fcmResponse.Error = ResponseError.UnknownError;
                            break;
                    }
                }
                return fcmResponse;
            }
        }

        private DownstreamMessageResponse ParseMessageResponse(WebResponse response)
        {
            using (var stream = response.GetResponseStream())
            {
                if (stream == null || !stream.CanRead)
                {
                    return null;
                }

                using (var reader = new StreamReader(stream))
                {
                    var data = reader.ReadToEnd();
                    var messageResponse = JsonConvert.DeserializeObject<DownstreamMessageResponse>(data, _jsonSettings);
                    return messageResponse;
                }
            }
        }

        private static string ParseStringResponse(WebResponse response)
        {
            using (var stream = response.GetResponseStream())
            {
                if (stream == null || !stream.CanRead)
                {
                    return null;
                }

                using (var reader = new StreamReader(stream))
                {
                    return reader.ReadToEnd();
                }
            }
        }
    }

    public class FcmResponse : IFcmResponse
    {
        public int ErrorCode { get; set; } = 200;
        public ResponseError Error { get; set; } = ResponseError.NoError;
        public string ErrorMessage { get; set; } = null;
        public DownstreamMessageResponse ResponseMessage { get; set; } = null;
    }

    public interface IFcmResponse
    {
        int ErrorCode { get; }
        ResponseError Error { get; }
        string ErrorMessage { get; }
        DownstreamMessageResponse ResponseMessage { get; }
    }

    public static class WebRequestExtensions
    {
        public static WebResponse GetResponseWithoutException(this WebRequest request)
        {
            try
            {
                return request.GetResponse();
            }
            catch (WebException e)
            {
                if (e.Response == null)
                {
                    throw;
                }

                return e.Response;
            }
        }
    }
}
