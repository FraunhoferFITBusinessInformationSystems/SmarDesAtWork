//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http;
using System.Text;
using Microsoft.Extensions.Logging;
using Xunit;
using Microsoft.AspNetCore.TestHost;
using Newtonsoft.Json;
using SmartDevicesGateway.Api;
using SmartDevicesGateway.Model.Jobs;
using Microsoft.Extensions.Configuration;
using System.IO;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.DependencyInjection;
using SmartDevicesGateway.Common.Config;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.UnitTests.Fixtures;
using IHostingEnvironment = Microsoft.Extensions.Hosting.IHostingEnvironment;

namespace SmartDevicesGateway.IntegrationTests
{
    public class ApiIntegrationTest : IClassFixture<ConfigFixture>, IDisposable
    {
        public TestServer Server { get; }
        public HttpClient Client { get; }
        public ConfigFixture Fixture { get; }

        public ApiIntegrationTest(ConfigFixture fixture)
        {
            Fixture = fixture;

            var contentRoot = ConfigurationPathUtils.ConfigurePathToContentRoot(false);

            var environment = "ApiTest";
            var configBuilder = new ConfigurationBuilder()
                .SetBasePath(contentRoot)
                .AddEnvironmentVariables()
                .AddJsonFile(Path.Combine(contentRoot, "config/ApplicationSettings.json"), optional: false, reloadOnChange: true)
                .AddJsonFile(Path.Combine(contentRoot, $"config/ApplicationSettings.{environment}.json"), optional: false, reloadOnChange: true);
            var configuration = configBuilder.Build();

            var builder = new WebHostBuilder()
                .UseEnvironment(environment)
                .UseConfiguration(configuration)
                .UseStartup<TestStartup>();

            Server = new TestServer(builder);
            Client = Server.CreateClient();
        }

        public void Dispose()
        {
            Server?.Dispose();
            Client?.Dispose();
        }


        protected T Get<T>(string uri) where T : class
        {
            var getResponse = Client.GetAsync(uri).Result;
            var responseString = getResponse.Content.ReadAsStringAsync().Result;

            // Assert
            Assert.NotNull(responseString);
            var getResponseObject = JsonConvert.DeserializeObject<T>(responseString);
            Assert.True(getResponse.StatusCode == HttpStatusCode.OK, getResponse.ReasonPhrase);
            Assert.NotNull(getResponseObject);

            return getResponseObject;
        }


        protected HttpResponseMessage Put<T>(string uri, T content) where T : class
        {
            var serializeObject = JsonConvert.SerializeObject(content, new JsonSerializerSettings
            {
                TypeNameHandling = TypeNameHandling.Auto,
                TypeNameAssemblyFormatHandling = TypeNameAssemblyFormatHandling.Simple
                //TypeNameHandling = TypeNameHandling.Auto,
                //SerializationBinder = new ShortClassnameSerializationBinder()
            });

            var putContent = new StringContent(serializeObject, Encoding.UTF8, "application/json");
            var putResponseResult = Client.PutAsync($"{uri}", putContent).Result;
            var putResponseContent = putResponseResult.Content.ReadAsStringAsync().Result;

            // Assert
            Assert.True(putResponseResult.StatusCode == HttpStatusCode.OK, putResponseResult.ReasonPhrase);

            return putResponseResult;
        }

        [Fact]
        public void TestGeneralServerResponse()
        {
            var uri = $"/api/echo";
            var getResponse = Client.GetAsync(uri).Result;

            // Assert
            Assert.True(getResponse.StatusCode == HttpStatusCode.OK, getResponse.ReasonPhrase);
        }

        [Fact]
        public void TestActionEndpoint()
        {
            Put("/api/actions/jobs/Alice.A", new Job
            {
                Type = "Job",
                Name = "jobKey",
                Resource = new Dictionary<string, string>
                {
                    // { "helpRequested", true }
                }
            });
        }

        [Fact]
        public void TestPutConfig()
        {
            var res = Get<DeviceInfo>("/api/config/info/Alice.A");

            Assert.NotNull(res);
            Assert.Null(res.FcmToken);

            var fcmToken = "foobar";

            Put("/api/config/info/Alice.A", new DeviceInfo
            {
                Id = "Alice",
                DeviceName = "A",
                FcmToken = fcmToken
            });

            res = Get<DeviceInfo>("/api/config/info/Alice.A");

            Assert.NotNull(res);
            Assert.Equal(fcmToken, res.FcmToken);
        }

        [Fact]
        public void TestSwitchUserInClient()
        {
            var uriAlice = "/api/config/info/Alice.A";
            var uriBob = "/api/config/info/Bob.A";
            var fcmToken = "foobar";
            var deviceInfoAlice = new DeviceInfo
            {
                Id = "Alice",
                DeviceName = "A",
                FcmToken = fcmToken
            };

            var deviceInfoBob = new DeviceInfo
            {
                Id = "Bob",
                DeviceName = "A",
                FcmToken = fcmToken
            };

            // Get Alice
            var res = Get<DeviceInfo>(uriAlice);
            Assert.Null(res.FcmToken);

            // Get Bob
            res = Get<DeviceInfo>(uriBob);
            Assert.Null(res.FcmToken);

            // Put Alice
            Put(uriAlice, deviceInfoAlice);
            res = Get<DeviceInfo>(uriAlice);
            Assert.Equal(fcmToken, res.FcmToken);

            // Get Bob
            res = Get<DeviceInfo>(uriBob);
            Assert.Null(res.FcmToken);

            // Put Bob
            Put(uriBob, deviceInfoBob);
            res = Get<DeviceInfo>(uriBob);
            Assert.Equal(fcmToken, res.FcmToken);

            //Get Alice
            res = Get<DeviceInfo>(uriAlice);
            Assert.Null(res.FcmToken);
        }

        
    }

    public class TestStartup : Startup
    {
        public TestStartup(IConfiguration configuration, ILoggerFactory loggerFactory, IHostingEnvironment hostingEnvironment) 
            : base(configuration, loggerFactory, hostingEnvironment)
        {
        }

        public override void ConfigureServices(IServiceCollection services)
        {
            var contentRoot = Configuration["contentRoot"] ?? ".";
            var dbPath = Path.Combine(contentRoot, "SmartDevicesGateway.db");

            try
            {
                File.Delete(dbPath);
            }
            catch
            {
                // ignored
            }

            base.ConfigureServices(services);
        }
    }
}