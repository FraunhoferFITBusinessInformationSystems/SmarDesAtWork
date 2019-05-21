//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Threading;
using log4net;
using log4net.Config;
using Microsoft.AspNetCore.Hosting;
using SmartDevicesGateway.Api;

[assembly: XmlConfigurator(ConfigFile = "config/log4net.config", Watch = true)]

namespace SmartDevicesGateway.NetCoreService
{  
    public class NetCoreService
    {
        private const string ProjectName = "SmartDevicesGateway";
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        public static void Main(string[] args)
        {
            var cancellationTokenSource = new CancellationTokenSource();
            GlobalContext.Properties["LogPath"] = Common.Logging.LoggingHelper.GetDefaultLogPathBySystem(ProjectName, "");
            GlobalContext.Properties["LogPathDebug"] = Common.Logging.LoggingHelper.GetDefaultLogPathBySystem(ProjectName, "debug");

            var runInConsole = args.Contains("--console");
            var argsWithoutConsole = args.Where(arg => arg != "--console").ToArray();

            try
            {
                if (Debugger.IsAttached || runInConsole)
                {
                    new Server().BuildWebHost(argsWithoutConsole, false).Run();
                }
                else
                {
                    new Server().BuildWebHost(argsWithoutConsole, true).RunAsync(cancellationTokenSource.Token).Wait(cancellationTokenSource.Token);
                }
            }
            catch (Exception e)
            {
                Logger.Error(e);
                if (Debugger.IsAttached)
                {
                    Console.Error.WriteLine($"[CRITICAL] Application stopped due to an Exception \"{e.Message}\"");
                    Console.Error.WriteLine($"Press any key to continue...");
                    Console.ReadKey();
                }
            }
        }
    }
}
