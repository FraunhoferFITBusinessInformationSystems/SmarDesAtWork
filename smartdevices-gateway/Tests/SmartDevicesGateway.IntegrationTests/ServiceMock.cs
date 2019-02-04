//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Amqp;
using SmartDevicesGateway.Model.Internal;
using Vogler.Amqp;

namespace SmartDevicesGateway.IntegrationTests
{
    public class ServiceMock
    {
        private readonly IAmqpService _amqpService;
        private readonly AmqpListener _amqpListener;

        public string QueueName => _amqpListener.QueueName;

        public IList<string> Messages { get; set; } = new List<string>();

        public string Name { get; set; }

        public ServiceMock(IAmqpService amqpService, string name, string queue = null)
        {
            _amqpService = amqpService;
            Name = name;

            _amqpListener = amqpService.GetAmqpListener(OnMessage, queue);
        }

        private bool OnMessage(Message message)
        {
            var messageBodyObj = AmqpUtils.DeserializeMessage(message.Body);
            if (messageBodyObj == null)
            {
                throw new Exception("Could not deserialize message");
            }

            try
            {
                switch (message.Properties.Subject)
                {
                    case "PutResource":
                    {
                        var data = messageBodyObj["body"].ToObject<string>();
                        var hashCode = Hash64Str(data);
                        _amqpService.SendMessage(message.Properties.ReplyTo, 
                            message.Properties.Subject, 
                            new { responseObject = new { hash = hashCode, uuid = Guid.NewGuid() } }, message.Properties.CorrelationId);
                        break;
                    }
                    default:
                        throw new NotImplementedException();
                }
            }
            catch (Exception e)
            {
                throw;
            }

            return true;
        }

        public static string Hash64Str(string base64Str)
        {
            var data = System.Convert.FromBase64String(base64Str);
            using (var hasher = System.Security.Cryptography.MD5.Create())
            {
                var hash = hasher.ComputeHash(data);
                var base64String = Convert.ToBase64String(hash);

                return base64String;
            }
        }
    }

}
