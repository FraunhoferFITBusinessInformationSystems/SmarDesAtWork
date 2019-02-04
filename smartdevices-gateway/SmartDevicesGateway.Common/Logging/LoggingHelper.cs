//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.IO;
using System.Runtime.InteropServices;

namespace SmartDevicesGateway.Common.Logging
{
    public class LoggingHelper
    {
        public static OSPlatform GetOSPlatform()
        {
            OSPlatform osPlatform = OSPlatform.Create("Other Platform");

            bool isWindows = RuntimeInformation.IsOSPlatform(OSPlatform.Windows);
            osPlatform = isWindows ? OSPlatform.Windows : osPlatform;

            bool isOSX = RuntimeInformation.IsOSPlatform(OSPlatform.OSX);
            osPlatform = isOSX ? OSPlatform.OSX : osPlatform;

            bool isLinux = RuntimeInformation.IsOSPlatform(OSPlatform.Linux);
            osPlatform = isLinux ? OSPlatform.Linux : osPlatform;

            return osPlatform;
        }

        public static string GetDefaultLogFolderBySystem(string applicationName)
        {
            if (GetOSPlatform() == OSPlatform.Windows)
            {
                return Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "VoglerEngineering", applicationName);
            }

            if (GetOSPlatform() == OSPlatform.Linux)
            {
                return Path.Combine("/var/log", "VoglerEngineering", applicationName);
            }

            if (GetOSPlatform() == OSPlatform.OSX)
            {
                return Path.Combine("/usr/local/var/log", "VoglerEngineering", applicationName);
            }

            return "./log/";
        }

        public static string GetDefaultLogName(string applicationName, string suffix)
        {
            if (string.IsNullOrWhiteSpace(suffix))
            {
                return $"{applicationName}.log";
            }
            return $"{applicationName}_{suffix}.log";
        }

        public static string GetDefaultLogPathBySystem(string applicationName, string suffix)
        {
            return Path.Combine(GetDefaultLogFolderBySystem(applicationName), GetDefaultLogName(applicationName, suffix));
        }
    }
}
