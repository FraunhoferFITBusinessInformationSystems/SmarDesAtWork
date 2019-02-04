//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using Amqp.Framing;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Messages;
using SmartDevicesGateway.Model.Messages.Enums;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Processing.Handler;
using Vogler.Amqp;
using Message = Amqp.Message;

namespace SmartDevicesGateway.Processing.Controller.SmartDevice
{
    public class ActionModel : AbstractModel
    {
        private readonly IPersistenceProvider _persistenceProvider;

        private readonly FcmMessageHandler _fcmMessageHandler;
        private readonly IAmqpService _amqpService;

        public ActionModel(ILoggerFactory loggerFactory, IPersistenceProvider persistenceProvider, FcmMessageHandler fcmMessageHandler,
            IAmqpService amqpService) : base(loggerFactory)
        {
            _persistenceProvider = persistenceProvider;
            _fcmMessageHandler = fcmMessageHandler;
            _amqpService = amqpService;
        }
        
        public bool StartNewJob(DeviceId deviceId, MessageReply reply)
        {
            reply.CreatedAt = DateTimeOffset.Now;
            _amqpService.SendMessage("RuleEngine", "Job", reply);

            return true;
        }

        // ReSharper disable once InconsistentNaming
        public Job TransformReadOnlyJob(DeviceId deviceId, Job job)
        {
            var refJob = _persistenceProvider.FindJobById(job.ReferenceId)?.Job;
            if (refJob == null)
            {
                return null;
            }

            refJob.Status = JobStatus.Done;
            _persistenceProvider.UpdateJobStatus(refJob.Id, JobStatus.Done);

            //Generate new Job with new GUID, no RefID, for Persistence
            var newJob = new Job
            {
                Id = Guid.NewGuid(),
                Name = "WzBruchMeldenRO",
                Type = "Job",
                ReferenceId = Guid.Empty,
                Status = JobStatus.InProgress,
                CreatedAt = job.CreatedAt,
                CreatedBy = job.CreatedBy,
                AssignedTo = job.CreatedBy,
                Immediate = false,
                Priority = job.Priority,
                Resource = job.Resource,
            };

            newJob.Resource.Add("list_title", "Werkzeugbruch");
            newJob.Resource.Add("list_text", $"Art-Nr.: {GetFromRes(newJob.Resource, "text")} - Maschine: {GetFromRes(newJob.Resource, "subject")}");

            _persistenceProvider.AddJob(deviceId, newJob);
            _fcmMessageHandler.SendGetDataWithoutNotification(deviceId);

            //Set new JobGUID to RUleEngine-Job and send it.
            job.Id = newJob.Id;
            job.ReferenceId = Guid.Empty;
            return job;
        }

        private static string GetFromRes(IReadOnlyDictionary<string, string> res, string key)
        {
            return res.ContainsKey(key) ? res[key] : "";
        }

        public void RemoveJob(DeviceId deviceId)
        {
            _persistenceProvider.RemoveJob(deviceId);
        }

        public void RemoveAllJobs()
        {
            _persistenceProvider.RemoveAllJobs();

            _fcmMessageHandler.SendGetDataWithoutNotification(_persistenceProvider.GetAllDeviceIds().ToArray());
        }

        public void PutJob(DeviceId deviceId, Job job)
        {
            // FIXME TODO Put this in Rule Engine
            if (job.Type.Equals("Job") && job.Name.Equals("WzBruchMelden"))
            {
                job = TransformReadOnlyJob(deviceId, job);
            }

            job.CreatedAt = DateTimeOffset.Now;

            _amqpService.SendMessage("RuleEngine", "Job", job);
        }
    }
}