//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using log4net;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Common.Interfaces;
using SmartDevicesGateway.Services.ConfigService.Providers;
using Vogler.Amqp;

namespace SmartDevicesGateway.Services.ConfigService
{
    public class ConfigService : IConfigService
    {
        private ILoggerFactory LoggerFactory { get; }
        private ILogger Logger { get; }

        private readonly Dictionary<Type, IConfigProvider> _fileReader = new Dictionary<Type, IConfigProvider>();

        public string ContentRoot { get; set; }
        public string Environment { get; set; }

        public ConfigService(ILoggerFactory loggerFactory)
        {
            LoggerFactory = loggerFactory;
            Logger = loggerFactory.CreateLogger<ConfigService>();
            Logger.LogDebug("Initializing ConfigService");
        }
        
        public void RegisterConfigFile(string file, Type type)
        {
            var configFile = file;
            if (!Path.IsPathRooted(configFile))
            {
                configFile = Path.Combine(ContentRoot, configFile);
            }
            
            try
            {
                if (Environment != null)
                {
                    var nameNoExtension = Path.GetFileNameWithoutExtension(configFile);
                    var extension = Path.GetExtension(configFile);
                    var environmentName = $"{nameNoExtension}.{Environment}{extension}";
                    var directory = Path.GetDirectoryName(configFile);
                    var environmentConfigFile = Path.Combine(directory ?? throw new ArgumentNullException(), environmentName);
                    if (File.Exists(environmentConfigFile))
                    {
                        configFile = environmentConfigFile;
                        Logger.LogInformation($"Environment configuration found in: \"{configFile}\"");
                    }
                }

                if (!File.Exists(configFile))
                {
                    throw new FileNotFoundException($"Unknown config file {file}. File not found.");
                }

                Logger.LogInformation($"Register configuration from File \"{configFile}\" as \"{type.Name}\"");
                var genericType = typeof(ConfigFileProvider<>).MakeGenericType(type);
                _fileReader[type] = (IConfigProvider)Activator.CreateInstance(genericType, LoggerFactory, configFile);
            }
            catch(Exception e)
            {
                Logger.LogInformation($"Could not Read configuration from {configFile}", e);
            }
        }

        public void RegisterAmqpConfig(Type type, IAmqpService amqpService, string queue, string configName)
        {
            try
            {
                Logger.LogInformation($"Register configuration from AMQP \"{queue}\" - \"{configName}\"");
                var genericType = typeof(ConfigAmqpProvider<>).MakeGenericType(type);
                _fileReader[type] = (IConfigProvider)Activator.CreateInstance(genericType, LoggerFactory, amqpService, queue, configName);
            }
            catch (Exception e)
            {
                Logger.LogInformation($"Could not register AMQP configuration {queue} - {configName}", e);
            }
        }
        
        public IConfigProvider<TConfig> GetProvider<TConfig>() where TConfig : class, IConfig
        {
            if(!_fileReader.ContainsKey(typeof(TConfig)))
            {
                return null;
            }

            var r = _fileReader[typeof(TConfig)];
            // ReSharper disable once SuspiciousTypeConversion.Global
            return r as IConfigProvider<TConfig>;
        }

        public TConfig Get<TConfig>() where TConfig : class, IConfig
        {
            return GetProvider<TConfig>()?.Get();
        }
    }
}
