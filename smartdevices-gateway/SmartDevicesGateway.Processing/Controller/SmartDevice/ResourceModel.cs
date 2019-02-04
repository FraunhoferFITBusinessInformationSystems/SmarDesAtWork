//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.ComponentModel.Design;
using System.IO;
using System.Linq;
using System.Net.Mime;
using System.Text;
using System.Threading.Tasks;
using System.Web;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using SmartDevicesGateway.Common;
using SmartDevicesGateway.Model.Resource;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Processing.Controller.Util;
using SmartDevicesGateway.Services.ConfigService;
using Vogler.Amqp;

namespace SmartDevicesGateway.Processing.Controller.SmartDevice
{
    public class ResourceModel : AbstractModel
    {
        private readonly IConfigService _configService;
        private readonly IAmqpService _amqpService;

        private string LinkServiceName { get; set; } = "resource";

        public ResourceModel(ILoggerFactory loggerFactory,
            IConfigService configService,
            IAmqpService amqpService
        ) : base(loggerFactory)
        {
            _configService = configService;
            _amqpService = amqpService;
        }

        /// <summary>
        /// Makes an asynchronous request over amqp for a specified resource.
        /// </summary>
        /// <param name="uri">Defines the requested element. The Host of the Uri is the Request queue,
        /// the query defines the Path and Name of the Resource.
        /// E.g. smartdevices://resource/files/test/sampleimage.jpg</param>
        /// <param name="etag">Provides an ETag of the requested element for cache-control, or null,
        /// if the item is not already cached.</param>
        /// <returns></returns>
        public Task<Tuple<ResourceInfoDto, Stream>> GetResource(Uri uri, string etag = null)
        {
            var type = uri.Segments.Skip(1).First().ToLower();
            string action;

            if (type.Equals("files/"))
            {
                action = "GetFile";

                return GetFileResource(uri, etag);
            }
            else if (type.Equals("images/"))
            {
                try
                {
                    var guid = Guid.Parse(uri.Segments.Last());

                    return GetImageResource(guid);
                }
                catch (Exception)
                {
                    // ignored
                    throw new BadRequestException();
                }
            }

            throw new NotFoundException();
        }

        public async Task<Tuple<ResourceInfoDto, Stream>> GetFileResource(Uri uri, string etag = null)
        {
            //ignore first stegment:
            string path;

            if (uri.Segments.Skip(1).First().Equals("files/"))
            {
                path = uri.Segments.ToList()
                    .Skip(2)
                    .Aggregate(new StringBuilder("/"),
                        (accu, i) => accu.Append(i)).ToString();
            }
            else
            {
                path = uri.LocalPath;
            }

            var decodedPath = HttpUtility.UrlDecode(path);

            var filename = uri.Segments.Last();
            var (jObject, stream) = await GetResource("Resources", "GetFile", new {path = decodedPath});
            var resourceInfo = new ResourceInfoDto()
            {
                Uri = uri,
                RequestPath = path,
                Name = filename,
            };

            if (jObject.ContainsKey("modificationTime"))
            {
                //parse modification time
                var millis = jObject["modificationTime"].ToObject<long>();
                var dateTime = DateTimeOffset.FromUnixTimeMilliseconds(millis);
                resourceInfo.LastModified = dateTime;
            }

            //parse mime type
            var mime = jObject?.GetValue("mimeType")?.ToString();
            resourceInfo.ContentType = ParseContentType(mime, filename);

            resourceInfo.ETag = jObject["etag"].ToString();
            resourceInfo.Size = jObject["size"].ToObject<long>();

            return new Tuple<ResourceInfoDto, Stream>(resourceInfo, stream);
        }

        public async Task<Tuple<ResourceInfoDto, Stream>> GetImageResource(Guid uuid)
        {
            var (jObject, stream) = await GetResource("Resources", "GetResource", new {uuid});
            var imageResource = new ResourceInfoDto();

            if (jObject.ContainsKey("uuid"))
            {
                //parse modification time
                var id = jObject["uuid"].ToObject<string>();
                imageResource.Uuid = Guid.Parse(id);
            }

            if (jObject.ContainsKey("name"))
            {
                //parse modification time
                var name = jObject["name"].ToObject<string>();
                imageResource.Name = name;
            }

            //parse mime type
            var mime = jObject.GetValue("mimeType")?.ToString();
            imageResource.ContentType = ParseContentType(mime, imageResource.Name);

            imageResource.ETag = jObject["etag"].ToString();
            imageResource.Size = jObject["size"].ToObject<long>();

            return new Tuple<ResourceInfoDto, Stream>(imageResource, stream);
        }

        protected Task<Tuple<JObject, Stream>> GetResource(string queue, string action, object requestBody)
        {
            var jResult = _amqpService.RequestReply(requestBody, action, queue);
            var response = AmqpApiUtils.ParseApiResult(jResult);

            if (!response.ResponseObject?.ContainsKey("body") ?? true)
            {
                throw new AmqpApiException("Could not read ApiResponse: body not set!");
            }

            var obj = response.ResponseObject;

            var data = System.Convert.FromBase64String(response.ResponseObject["body"].ToObject<string>());
            obj["body"] = null;
            obj["size"] = data.LongLength;
            obj["etag"] = GenerateEtag(data);
            var ms = new MemoryStream(data);

            return Task.FromResult(new Tuple<JObject, Stream>(obj, ms));
        }

        public async Task<Guid> PostImageResource(ResourceInfoDto res, Stream uploadStream)
        {
            var requestBody = new
            {
                name = res.Name,
                mimeType = res.ContentType.ToString()
            };
            var response = await PostResource("Resources", "PutResource", requestBody, uploadStream);

            if (response.ResponseObject?.ContainsKey("uuid") ?? false)
            {
                return Guid.Parse(response.ResponseObject["uuid"].ToString());
            }
            else
            {
                throw new AmqpApiResponseException("Could not read parameters from Amqp Response");
            }
        }

        private Task<AmqpApiResponse> PostResource(string queue, string action, object resourceBody,
            Stream uploadStream)
        {
            var jObject = JObject.FromObject(resourceBody);

            var ms = new MemoryStream();
            uploadStream.CopyTo(ms);
            var base64String = Convert.ToBase64String(ms.ToArray());
            jObject["body"] = base64String;

            var jResult = _amqpService.RequestReply(jObject, action, queue);
            return Task.FromResult(AmqpApiUtils.ParseApiResult(jResult));
        }

        private static string GenerateEtag(byte[] data)
        {
            using (var hasher = System.Security.Cryptography.MD5.Create())
            {
                var hash = hasher.ComputeHash(data);
                var base64String = Convert.ToBase64String(hash);

                return base64String;
            }
        }

        private static ContentType ParseContentType(string contentType, string filename = null)
        {
            ContentType type = null;

            if (contentType != null)
            {
                try
                {
                    type = new ContentType(contentType);
                }
                catch (Exception)
                {
                    // ignored
                }
            }

            if (type == null && filename != null)
            {
                //Special cases for unknown mime-type
                //TODO: Resource server cannot identify json as content type.
                //Patching this here
                if (filename.EndsWith(".json"))
                {
                    type = new ContentType("text/json");
                }
            }

            return type ?? (type = new ContentType("application/octet-stream"));
        }
    }

    public class ResourceInfoDto
    {
        public string Name { get; set; }
        public string ETag { get; set; }
        public long Size { get; set; }

        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public Uri Uri { get; set; }

        [JsonProperty("Content-Type", NullValueHandling = NullValueHandling.Ignore)]
        public ContentType ContentType { get; set; }

        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public DateTimeOffset? LastModified { get; set; }

        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public string RequestPath { get; set; }

        [JsonProperty(NullValueHandling = NullValueHandling.Ignore)]
        public Guid? Uuid { get; set; }
    }
}