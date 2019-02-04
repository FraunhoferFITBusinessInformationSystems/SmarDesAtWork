//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Amqp;
using Amqp.Framing;
using Amqp.Types;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using SmartDevicesGateway.Common.Interfaces;
using SmartDevicesGateway.Model.Amqp;
using SmartDevicesGateway.Model.Amqp.Enums;
using Vogler.Amqp;

namespace SmartDevicesGateway.Services.ConfigService.Providers
{
    public class ConfigAmqpProvider<T> : AbstractConfigProvider<T> where T : class, IConfig
    {
        private readonly Type _type;
        private readonly IAmqpService _amqpService;
        private readonly string _queue;
        private readonly string _configName;

        private AmqpConnection _amqpConnection;

        public ConfigAmqpProvider(Type type, ILoggerFactory loggerFactory, IAmqpService amqpService, 
            string queue, string configName) : base(loggerFactory)
        {
            _type = type;
            _amqpService = amqpService;
            _queue = queue;
            _configName = configName;

            StartListen();
            SendConfigRequest();
        }

        private void SendConfigRequest()
        {
            var request = new AmqpResourceRequest()
            {
                Action = ResourceAction.Read,
                ResponsePath = _queue,
                Resource = _configName
            };

            var jsonTxt = JsonConvert.SerializeObject(request);
            var body = Encoding.UTF8.GetBytes(jsonTxt);

            var msg = new Message
            {
                Properties = new Properties(),
                ApplicationProperties = new ApplicationProperties(),
                Header = new Header(),
                BodySection = new Data
                {
                    Binary = Encoding.UTF8.GetBytes(jsonTxt)
                }
            };

            //            _amqpService.SendMessage("ConfigRequest", "sdgw-config-request", request);
            throw new NotImplementedException();
            //TODO obsolete
        }

        private void StartListen()
        {
//            _amqpConnection = _amqpService.GetConnection(_queue);
//            _amqpConnection.MessageReceiver += MessageReceiver;
//            _amqpConnection.StartListening();
            throw new NotImplementedException();
            //TODO obsolete
        }

        private bool MessageReceiver(Message message)
        {
            if (!string.Equals(message.Properties?.Subject, _configName, StringComparison.Ordinal))
            {
                return false;
            }

            if (!(message.Body is byte[] bytes))
            {
                return false;
            }

            try
            {
                var jsonString = Encoding.UTF8.GetString(bytes);
                var config = JsonConvert.DeserializeObject<T>(jsonString);

                if (config == null)
                {
                    throw new ApplicationException("Could not parse config Object");
                }

                //Update and notify listeners
                Config = config;

                return true;
            }
            catch (Exception e)
            {
                Logger.LogError(e, "Error while processing config-resource-response");
                return true;
            }
        }
    }
}
