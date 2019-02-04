//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Amqp;
using log4net;
using Microsoft.Extensions.Configuration;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Model.Values;
using SmartDevicesGateway.Processing.Handler;
using Vogler.Amqp;

namespace SmartDevicesGateway.Api.HostedServices
{
    public class AmqpListenerService : BackgroundService
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
        private readonly IPersistenceProvider _PersistenceProvider;
        private readonly FcmMessageHandler _FcmMessageHandler;

        private AmqpListener _AmqpListener;
        private string _queueName;

        public AmqpListenerService(IPersistenceProvider persistenceProvider, FcmMessageHandler fcmMessageHandler,
            IConfiguration configuration) 
        {
            _PersistenceProvider = persistenceProvider;
            _FcmMessageHandler = fcmMessageHandler;
            
            var connectionString = configuration.GetValue<string>("AmqpServiceConfig:ConnectionString") 
                                   ?? throw new Exception("Config item AmqpServiceConfig:ConnectionString is missing.");
            var clientName = configuration.GetValue<string>("AmqpServiceConfig:ClientName") 
                             ?? throw new Exception("Config item AmqpServiceConfig:ClientName is missing.");
            var queueName = configuration.GetValue<string>("AmqpServiceConfig:QueueName") 
                            ?? throw new Exception("Config item AmqpServiceConfig:QueueName is missing.");

            _AmqpListener = new AmqpListener(connectionString, clientName, queueName, OnMessageReceived);
            _queueName = queueName; //For logging purposes
        }

        protected override Task ExecuteAsync(CancellationToken stoppingToken)
        {
            return Task.CompletedTask;
        }

        private bool OnMessageReceived(Message message)
        {
            try
            {
                Logger.DebugLogAmqpMessage($"Incomming AMQP Message on queue \"{_queueName}\": ", message);
                var jsonTxt = GetJsonBody(message);
                //Logger.Debug($"Incoming AMQP Message:\n{PrintAmqpMessage(jsonTxt)}");

                jsonTxt = jsonTxt.Replace("Content", "Resource");

                var jObject = JObject.Parse(jsonTxt);

                var name = (string) jObject["Name"];
                var id = (string) jObject["Id"];
                var refId = (string) jObject["ReferenceId"];
                var assignedTo = (string) jObject["AssignedTo"];
                var deviceId = DeviceId.TryParse(assignedTo);
                var type = (string) jObject["Type"];

                if ((type == "Job" || type == "TodoList") && deviceId != null)
                {
                    var immediate = (bool) jObject["Immediate"];

                    var job = JsonConvert.DeserializeObject<Job>(jsonTxt);
                    Logger.Debug($"Valid Job received: {job.Name} - {job.Id} - {job.AssignedTo}");

                    Logger.Info($"Job({job.Name} => {deviceId.FullId}");

                    _PersistenceProvider.AddJob(deviceId, job);

                    if (immediate)
                    {
                        var token = jObject["NotificationPattern"];
                        if (token != null)
                        {
                            var pattern = token.ToObject<int>();
                            _FcmMessageHandler.SendGetDataWithNotification(pattern, deviceId);
                        }
                        else
                        {
                            _FcmMessageHandler.SendGetDataWithNotification(deviceId);
                        }
                    }
                    else
                    {
                        _FcmMessageHandler.SendGetDataWithoutNotification(deviceId);
                    }
                }
                else if (type == "Notification")
                {
                    var statusStr = (string) jObject["Status"];

                    if (name == "StatusUpdate" && Enum.TryParse(statusStr, true, out JobStatus status))
                    {
                        Logger.Debug($"Valid Job Status update received: Id:{id}, Status: {status}");

                        _PersistenceProvider.UpdateJobStatus(Guid.Parse(id), status);
                        var list = _PersistenceProvider.GetAllJobs()
                            .Where(j => j != null && j.Id.Equals(Guid.Parse(id))).ToList();

                        foreach (var job in list)
                        {
                            DeviceId receiver;

                            if (job.AssignedTo != null)
                            {
                                receiver = DeviceId.TryParse(job.AssignedTo);
                            }
                            else if (job.CreatedBy != null)
                            {
                                receiver = DeviceId.TryParse(job.CreatedBy);
                            }
                            else
                            {
                                throw new Exception("Unknown Notification-Receiver!");
                            }

                            Logger.Info($"Notification({job.Name}) => {receiver.FullId}");
                            _FcmMessageHandler.SendGetDataWithoutNotification(receiver);
                        }
                    }
                    else
                    {
                        //Ignore other Notifications
                        Logger.Error("Unknown Notification from Amqp");
                    }
                }
                else if (type == "ValueMessage")
                {
                    var valueName = (string) jObject["ValueName"];

                    _PersistenceProvider.AddValue(valueName, new Value
                    {
                        Name = valueName,
                        Val = jObject
                    });
                }

                return true;
            }
            catch (Exception e)
            {
                Logger.Error("Exception in OnMessageReceived!", e);
            }

            return true;
        }

        private string GetRes(Dictionary<string, string> res, string key)
        {
            return res.ContainsKey(key) ? res[key] : null;
        }

        private string GetJsonBody(Message message)
        {
            if (!(message.Body is byte[] bytes))
            {
                return null;
            }

            return Encoding.UTF8.GetString(bytes);
        }
    }
}