//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common.Interfaces;

namespace SmartDevicesGateway.Services.ConfigService.Providers
{
    public class ConfigFileProvider<T> : AbstractConfigProvider<T> where T : class, IConfig
    {
        private readonly string _configFile;
        private FileSystemWatcher _configFileWatcher;
        private DateTime _lastRead = DateTime.MinValue;

        public ConfigFileProvider(ILoggerFactory loggerFactory, string configFile) : base(loggerFactory)
        {
            _configFile = configFile;

            SetupFileWatcher();
            ReadConfig(_configFile);
        }

        public ConfigFileProvider(ILoggerFactory loggerFactory) : base(loggerFactory)
        {
            _configFile = null;
        }

        private void SetupFileWatcher()
        {
            Logger.LogDebug($"Registrating filesystem-watcher for configfile: {_configFile}");
            _configFileWatcher = new FileSystemWatcher
            {
                Path = Path.GetDirectoryName(_configFile),
                NotifyFilter = NotifyFilters.LastWrite,
                Filter = Path.GetFileName(_configFile)
            };

            _configFileWatcher.Changed += FileChanged;
            _configFileWatcher.EnableRaisingEvents = true;
        }

        private void FileChanged(object sender, FileSystemEventArgs args)
        {
            var path = args != null ? args.FullPath : _configFile;
            var lastWriteTime = File.GetLastWriteTime(path);
            var difference = lastWriteTime - _lastRead;
            if (difference >= TimeSpan.FromSeconds(1)) // Prevent from calling multiple times 
            {
                _lastRead = lastWriteTime;  
                ReadConfig(path);
            }
        }

        public void ReadConfig(TextReader reader)
        {
            try
            {
                Logger.LogInformation("Reading config file...");
                var obj = ReadConfigFile<T>(reader);

                Config = obj ?? throw new NullReferenceException("Error while Reading file.");
            }
            catch (Exception e)
            {
                Logger.LogError("Exception while reading config.", e);
            }
        }

        public void ReadConfig(string configFile)
        {
            if (!WaitReady(configFile))
            {
                return;
            }

            using (var r = new StreamReader(configFile))
            {
                ReadConfig(r);
            }
        }

        public void ReadConfig()
        {
            ReadConfig(_configFile);
        }

        public static TConfig ReadConfigFile<TConfig>(TextReader reader) where TConfig : class, IConfig
        {
            var serializer = new Newtonsoft.Json.JsonSerializer();
            //serializer.Converters.Add(new ConfigInterfaceJsonConverter());

            var obj =
                serializer.Deserialize(reader, typeof(TConfig)) as TConfig;

            return obj;
        }

        public static Dictionary<string, IConfig> ReadConfigFile(TextReader reader)
        {
            var serializer = new Newtonsoft.Json.JsonSerializer();
            //serializer.Converters.Add(new ConfigInterfaceJsonConverter());

            var entities =
                serializer.Deserialize(reader, typeof(Dictionary<string, IConfig>)) as Dictionary<string, IConfig>;

            return entities;
        }

        private bool WaitReady(string fileName)
        {
            const int retries = 10;
            var counter = 0;
            Exception e = null;
            while (counter <= retries)
            {
                try
                {
                    using (Stream stream = File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.None))
                    {
                        if (stream != null)
                        {
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    e = ex;
                }

                counter++;
                Thread.Sleep(500);
            }
            Logger.LogError(e, "Error while reading config file!");
            return false;
        }
    }
}
