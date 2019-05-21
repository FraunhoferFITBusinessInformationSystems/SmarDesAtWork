//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using log4net;
using log4net.Config;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Hosting.WindowsServices;
using SmartDevicesGateway.Api;

[assembly: XmlConfigurator(ConfigFile = "config/log4net.config", Watch = true)]

namespace SmartDevicesGateway.WindowsService
{
    public class WindowsService
    {
        private const string ProjectName = "SmartDevicesGateway";
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        static void Main(string[] args)
        {
            try
            {
                GlobalContext.Properties["LogPath"] = Common.Logging.LoggingHelper.GetDefaultLogPathBySystem(ProjectName, "");
                GlobalContext.Properties["LogPatDebug"] = Common.Logging.LoggingHelper.GetDefaultLogPathBySystem(ProjectName, "debug");

                var runInConsole = args.Contains("--console");
                var argsWithoutConsole = args.Where(arg => arg != "--console").ToArray();

                if (Debugger.IsAttached || runInConsole)
                {
                    new Server().BuildWebHost(argsWithoutConsole, false).Run();
                }
                else
                {
                    new Server().BuildWebHost(argsWithoutConsole, true).RunAsService();
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
