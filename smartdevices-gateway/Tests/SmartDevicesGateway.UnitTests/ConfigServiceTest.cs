//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Services;
using SmartDevicesGateway.Services.ConfigService;
using SmartDevicesGateway.Services.ConfigService.Providers;
using SmartDevicesGateway.TestCommon;
using SmartDevicesGateway.UnitTests.Fixtures;
using Xunit;

namespace SmartDevicesGateway.UnitTests
{
    [Trait("Category", Categories.UNIT)]
    public class ConfigServiceTest : IClassFixture<LoggingFixture>
    {
        public LoggingFixture Fixture { get; }

        public ConfigServiceTest(LoggingFixture fixture)
        {
            Fixture = fixture;
            Fixture.LoggerFactory.CreateLogger<ConfigServiceTest>();
        }

        /// <summary>
        /// Test simple serialize and deserialize behavior of the <see cref="ConfigFileProvider{T}"/>
        /// </summary>
        [Fact]
        public void TestConfigFileProviderSerialization()
        {
            //Create JSON
            var data = new TestServiceConfig
            {
                TestServiceAConfig = new TestServiceAConfig() {A = "AAA"},
                TestServiceBConfig = new TestServiceBConfig() {B = "BBB"},
                TestServiceCConfig = new TestServiceCConfig() {C = "CCC"}
            };
            
            var r = new ConfigFileProvider<TestServiceConfig>(Fixture.LoggerFactory);

            using (var mStream = new MemoryStream())
            {
                var writer = new StreamWriter(mStream, Encoding.UTF8);
                var serializer = new Newtonsoft.Json.JsonSerializer();
                //serializer.Converters.Add(new ConfigInterfaceJsonConverter());
                //serializer.ReferenceLoopHandling = ReferenceLoopHandling.Ignore;

                serializer.Serialize(writer, data);

                writer.Flush();
                mStream.Position = 0;
                var reader = new StreamReader(mStream, Encoding.UTF8);
                r.ReadConfig(reader);
                Assert.NotNull(r.Get());

                Assert.Equal(data.TestServiceAConfig.A, r.Get().TestServiceAConfig.A);
                Assert.Equal(data.TestServiceBConfig.B, r.Get().TestServiceBConfig.B);
                Assert.Equal(data.TestServiceCConfig.C, r.Get().TestServiceCConfig.C);
            }
        }

        [Fact]
        public void TestConfigFileReaderAndFileWatcher()
        {
            var file = Path.Combine(Fixture.ContentRoot, "config", $"{Guid.NewGuid()}.json");

            //Create JSON
            var data = new TestServiceConfig
            {
                TestServiceAConfig = new TestServiceAConfig() { A = "AAA" },
                TestServiceBConfig = new TestServiceBConfig() { B = "BBB" },
                TestServiceCConfig = new TestServiceCConfig() { C = "CCC" }
            };

            WriteConfigFile(file, data);

            var provider = new ConfigFileProvider<TestServiceConfig>(Fixture.LoggerFactory, file);
            
            Assert.NotNull(provider.Get());
            Assert.Equal(data.TestServiceAConfig.A, provider.Get().TestServiceAConfig.A);
            Assert.Equal(data.TestServiceBConfig.B, provider.Get().TestServiceBConfig.B);
            Assert.Equal(data.TestServiceCConfig.C, provider.Get().TestServiceCConfig.C);

            var configChangeEventTriggered = 0;

            //Now: Register Change listener and edit the file.
            provider.ConfigChangedEvent += (sender, args) =>
            {
                configChangeEventTriggered++;
                Assert.NotNull(args.NewValue);
                Assert.NotEqual("AAA", provider.Get().TestServiceAConfig.A);
                Assert.Equal("BBB", provider.Get().TestServiceBConfig.B);
                Assert.Equal("CCC", provider.Get().TestServiceCConfig.C);
            };

            data.TestServiceAConfig.A = "foo";
            WriteConfigFile(file, data);

            var maxIter = 0;
            while (configChangeEventTriggered == 0 && maxIter < 20)
            {
                maxIter++;
                Thread.Sleep(500);
            }

            Assert.Equal(1, configChangeEventTriggered);
        }

        [Fact]
        public void TextConfigServiceGeneral()
        {
            var service = new ConfigService(Fixture.LoggerFactory)
            {
                ContentRoot = Fixture.ContentRoot
            };

            try
            {
                service.RegisterConfigFile(Path.Combine("config", "TestServiceConfig.json"), typeof(TestServiceConfig));
            }
            catch (Exception e)
            {
                Assert.NotNull(e);
            }
            
            var configFileReader = service.GetProvider<TestServiceConfig>();
            Assert.NotNull(configFileReader);
            var config = configFileReader.Get();
            Assert.NotNull(config);
            Assert.Equal("AAA", config.TestServiceAConfig.A);
            Assert.Equal("BBB", config.TestServiceBConfig.B);
            Assert.Equal("CCC", config.TestServiceCConfig.C);
        }

        private static void WriteConfigFile(string file, TestServiceConfig data)
        {
            using (var fileWriter = new StreamWriter(File.OpenWrite(file)))
            {
                var serializer = new Newtonsoft.Json.JsonSerializer();
                serializer.Serialize(fileWriter, data);
                fileWriter.Flush();
            }
        }
    }
}
