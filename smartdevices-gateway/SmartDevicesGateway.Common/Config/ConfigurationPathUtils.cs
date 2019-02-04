//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Text;

namespace SmartDevicesGateway.Common.Config
{
    public class ConfigurationPathUtils
    {
        private ConfigurationPathUtils()
        {
        }

        public static string ConfigurePathToContentRoot(bool isService)
        {
            string pathToContentRoot;
            if (isService)
            {
                pathToContentRoot = AppDomain.CurrentDomain.BaseDirectory;
                return pathToContentRoot;
            }
            pathToContentRoot = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            //pathToContentRoot = AppDomain.CurrentDomain.BaseDirectory;
            return pathToContentRoot;
        }
    }

}
