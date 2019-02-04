//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace SmartDevicesGateway.TestCommon
{
    public class JsonEndpointClient
    {
        public HttpClient Client { get; set; }

        protected virtual ApiResponse<T> Get<T>(string uri) where T : class
        {
            try
            {
                var getResponse = Client.GetAsync(uri).Result;
                return ApiResponse<T>.Create<T>(getResponse);
            }
            catch (Exception e)
            {
                return ApiResponse<T>.Create<T>(null, e);
            }
        }

        protected virtual async Task<ApiResponse<T>> GetAsync<T>(string uri) where T : class
        {
            try
            {
                var getResponse = await Client.GetAsync(uri);
                return await ApiResponse<T>.CreateAsync<T>(getResponse);
            }
            catch (Exception e)
            {
                return await ApiResponse<T>.CreateAsync<T>(null, e);
            }
        }

        protected virtual ApiResponse<IEnumerable<T>> GetAll<T>(string uri) where T : class
        {
            try
            {
                var getResponse = Client.GetAsync(uri).Result;
                return ApiResponse<IEnumerable<T>>.CreateMany<T>(getResponse);
            }
            catch (Exception e)
            {
                return ApiResponse<IEnumerable<T>>.CreateMany<T>(null, e);
            }
        }

        protected virtual async Task<ApiResponse<IEnumerable<T>>> GetAllAsync<T>(string uri) where T : class
        {
            try
            {
                var getResponse = await Client.GetAsync(uri);
                return await ApiResponse<IEnumerable<T>>.CreateManyAsync<T>(getResponse);
            }
            catch (Exception e)
            {
                return await ApiResponse<IEnumerable<T>>.CreateManyAsync<T>(null, e);
            }
        }

        protected virtual ApiResponse<T> Post<T>(string uri, T content) where T : class
        {
            var postContent = new StringContent(JsonConvert.SerializeObject(content), Encoding.UTF8, "application/json");
            
            try
            {
                var getResponse = Client.PostAsync(uri, postContent).Result;
                return ApiResponse<T>.Create<T>(getResponse);
            }
            catch (Exception e)
            {
                return ApiResponse<T>.Create<T>(null, e);
            }
        }

        protected virtual IEnumerable<ApiResponse<T>> PostMany<T>(string uri, IEnumerable<T> content) where T : class
        {
            var responses = new List<ApiResponse<T>>();
            responses.AddRange(content.Select(x => Post(uri, x)));
            return responses;
        }

        protected virtual ApiResponse<T> Put<T>(string uri, T content) where T : class
        {
            var putContent = new StringContent(JsonConvert.SerializeObject(content), Encoding.UTF8, "application/json");
            try
            {
                var getResponse = Client.PutAsync(uri, putContent).Result;
                return ApiResponse<T>.Create<T>(getResponse);
            }
            catch (Exception e)
            {
                return ApiResponse<T>.Create<T>(null, e);
            }
        }

        protected virtual async Task<ApiResponse<T>> PutAsync<T>(string uri, T content) where T : class
        {
            var putContent = new StringContent(JsonConvert.SerializeObject(content), Encoding.UTF8, "application/json");
            try
            {
                var getResponse = await Client.PutAsync(uri, putContent);
                return await ApiResponse<T>.CreateAsync<T>(getResponse);
            }
            catch (Exception e)
            {
                return await ApiResponse<T>.CreateAsync<T>(null, e);
            }
        }

        protected virtual ApiResponse<T> Delete<T>(string uri) where T : class
        {
            try
            {
                var getResponse = Client.DeleteAsync(uri).Result;
                return ApiResponse<T>.Create<T>(getResponse);
            }
            catch (Exception e)
            {
                return ApiResponse<T>.Create<T>(null, e);
            }
        }

        protected virtual async Task<ApiResponse<FileDownloadResult>> GetFileAsync(string uri, string downloadDir,
            int fileStreamBufferSize = 4096)
        {
            HttpResponseMessage response = null;

            try
            {
                response = await Client.GetAsync(uri);
                var content = response.Content;
                var filename = content.Headers?.ContentDisposition?.FileName;

                if (filename == null)
                {
                    filename = DateTimeOffset.Now.ToString("yyyyMMddHHmmss") + "_" + new Random().Next(999999);
                }

                var contentType = content.Headers?.ContentType;
                if (contentType == null)
                {
                    contentType = MediaTypeHeaderValue.Parse("application/octet-stream");
                }

                var filePath = Path.Combine(downloadDir, filename);

                using (
                    Stream contentStream = await content.ReadAsStreamAsync(),
                    stream = new FileStream(filePath, FileMode.Create, FileAccess.Write, FileShare.None,
                        fileStreamBufferSize, true))
                {
                    await contentStream.CopyToAsync(stream);
                }

                return new ApiResponse<FileDownloadResult> { 
                    StatusCode = response.StatusCode,
                    Result = new FileDownloadResult {
                        ContentType = contentType.ToString(),
                        File = filePath,
                        Headers = content.Headers
                    },
                    Error = null,
                    RawJson = null
                };
            }
            catch (Exception e)
            {
                return new ApiResponse<FileDownloadResult>
                {
                    StatusCode = response?.StatusCode ?? HttpStatusCode.OK,
                    Result = null,
                    Error = e,
                    RawJson = null
                };
            }
        }
    }

    public class FileDownloadResult
    {
        public string File { get; set; }
        public string ContentType { get; set; }

        public HttpContentHeaders Headers { get; set; }
    }

    public class ApiResponse<TResult>
    {
        public ApiResponse()
        {
        }

        public ApiResponse(TResult result, HttpStatusCode statusCode, string rawJson, Exception error)
        {
            Result = result;
            StatusCode = statusCode;
            RawJson = rawJson;
            Error = error;
        }

        public TResult Result { get; set; }
        public HttpStatusCode StatusCode { get; set; }
        public string RawJson { get; set; }
        public Exception Error { get; set; }

        public bool HasError => Error != null;

        public static ApiResponse<T> Create<T>(HttpResponseMessage msg, Exception error = null) where T : class
        {
            string raw = null;
            var t = default(T);

            try
            {
                raw = msg.Content.ReadAsStringAsync().Result;
                t = Deserialize<T>(raw);
            }
            catch (Exception e)
            {
                if(error != null)
                {
                    error = e;
                }
            }
            return new ApiResponse<T>(t, msg.StatusCode, raw, error);
        }

        public static async Task<ApiResponse<T>> CreateAsync<T>(HttpResponseMessage msg, Exception error = null) where T : class
        {
            string raw = null;
            var t = default(T);

            try
            {
                raw = await msg.Content.ReadAsStringAsync();
                t = Deserialize<T>(raw);
            }
            catch (Exception e)
            {
                if (error != null)
                {
                    error = e;
                }
            }
            return new ApiResponse<T>(t, msg.StatusCode, raw, error);
        }

        public static ApiResponse<IEnumerable<T>> CreateMany<T>(HttpResponseMessage msg, Exception error = null) where T : class
        {
            string raw = null;
            var t = default(IEnumerable<T>);

            try
            {
                raw = msg.Content.ReadAsStringAsync().Result;
                t = Deserialize<IEnumerable<T>>(raw);
            }
            catch (Exception e)
            {
                if (error != null)
                {
                    error = e;
                }
            }
            return new ApiResponse<IEnumerable<T>>(t, msg.StatusCode, raw, error);
        }

        public static async Task<ApiResponse<IEnumerable<T>>> CreateManyAsync<T>(HttpResponseMessage msg, Exception error = null) where T : class
        {
            string raw = null;
            var t = default(IEnumerable<T>);

            try
            {
                raw = await msg.Content.ReadAsStringAsync();
                t = Deserialize<IEnumerable<T>>(raw);
            }
            catch (Exception e)
            {
                if (error != null)
                {
                    error = e;
                }
            }
            return new ApiResponse<IEnumerable<T>>(t, msg.StatusCode, raw, error);
        }

        public static T Deserialize<T>(string json) where T : class
        {
            return JsonConvert.DeserializeObject<T>(json);
//            try
//            {
//                
//            }
//            catch (Exception)
//            {
//                return default(T);
//            }
        }
    }
}
