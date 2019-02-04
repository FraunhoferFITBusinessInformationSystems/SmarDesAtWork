//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Threading;
using log4net;
using log4net.Config;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common.Config;

namespace SmartDevicesGateway.Api
{
    public class Server
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
        private readonly CancellationTokenSource _cancellationTokenSource = new CancellationTokenSource();

        public IWebHost BuildWebHost(string[] args, bool isService)
        {
            var logRepository = LogManager.GetRepository(Assembly.GetEntryAssembly());
            XmlConfigurator.Configure(logRepository, new FileInfo("config/log4net.config"));

            //Parse Application Arguments
            var argConfig = new ConfigurationBuilder()
                .AddCommandLine(args).Build();

            var environment = "Development";
            var env = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT");
            if (env != null && env.Any())
            {
                environment = env;
            }
            env = argConfig["environment"];
            if (env != null && env.Any())
            {
                environment = env;
            }

            var pathToContentRoot = ConfigurationPathUtils.ConfigurePathToContentRoot(isService);

            Logger.Info($"pathToContentRoot = {pathToContentRoot}");
            Logger.Info($"environmentName = {environment}");
            Logger.Info($"isService = {isService}");

            var builder = new ConfigurationBuilder()
                .SetBasePath(pathToContentRoot)
                .AddJsonFile("config/ApplicationSettings.json", optional: false, reloadOnChange: true)
                .AddJsonFile($"config/ApplicationSettings.{environment}.json", optional: true, reloadOnChange: true)
                .AddEnvironmentVariables()
                .AddCommandLine(args);
            var configuration = builder.Build();

            var serverUrlsSection = configuration.GetSection("ServerUrls");

            var serverUrlsItems = serverUrlsSection.AsEnumerable();
            var urls = from kvp in serverUrlsItems
                where !string.IsNullOrWhiteSpace(kvp.Value)
                select kvp.Value;

            var host = new WebHostBuilder()
                .CaptureStartupErrors(true) // the default
                .UseSetting("detailedErrors", "true")
                .UseKestrel(options => options.AddServerHeader = false)
                .UseUrls(urls.ToArray())
                .UseContentRoot(pathToContentRoot)
                .UseStartup<Startup>()
                .UseConfiguration(configuration)
                .UseEnvironment(environment)
                .Build();

            return host;
        }
    }
}
