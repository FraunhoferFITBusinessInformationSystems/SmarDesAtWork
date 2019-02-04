//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Threading.Tasks;
using log4net;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Api.Requests;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Jobs.Enums;
using SmartDevicesGateway.Model.Messages;
using SmartDevicesGateway.Model.Messages.Enums;
using SmartDevicesGateway.TestCommon;

namespace SmartDevicesGateway.LoadTest.SimpleChat
{
    /// <summary>
    /// This class polls the API for new Messages and sends apropriate answers for these open Messages.
    /// </summary>
    public class MessageAnswerClient : MessageSendingQueue
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        public Stopwatch Ticker { get; set; }
        public string TabName { get; set; } = "dashboard";
        public TimeSpan PollingDelay { get; set; }
        public Func<DeviceId> TargetDevice { get; set; }

        public int MessagesConfirmed { get; private set; } = 0;
        public int MessagesSent { get; private set; } = 0;
        public int JobsStarted { get; private set; } = 0;

        private bool _pollingActive = false;
        private Task _pollingTask;

        private readonly HashSet<Guid> _knownIds = new HashSet<Guid>();

        public MessageAnswerClient(SdgwEndpointClient client) : base(client)
        {
        }

        public void StartPollingForMessages()
        {
            _pollingTask = new Task(PollForMessages);
            _pollingTask.Start();
        }

        private void PollForMessages()
        {
            _pollingActive = true;
            while (_pollingActive)
            {
                var res = ReadAndProcessJobs();
                res.Wait();
                Task.Delay(PollingDelay).Wait();
            }
        }

        public void StopPollingForMessages()
        {
            _pollingActive = false;
        }

        private async Task ReadAndProcessJobs()
        {
            try
            {
                var apiResponse = await Client.GetDataTab(DeviceId, TabName, new PaginationRequestDto
                {
                    PageSize = int.MaxValue,
                }, null, new AdvancedFilterRequestDto()
                {
                    FilterBy = new[] {"status"},
                    FilterExcluding = new[] {true},
                    FilterString = new[] {"Done"}
                });

                if (apiResponse.HasError || apiResponse.Result == null)
                {
                    Logger.Error(
                        $"Error encountered on 'ReadAndProcessJobs'. Status Code: {apiResponse.StatusCode.ToString()}",
                        apiResponse.Error);

                    return;
                }

                var jobs = apiResponse.Result;
                var newJobs = jobs.Where(x => !_knownIds.Contains(x.Entry.Id)).ToList();

                if(newJobs.Count != 0)
                {
                    Logger.Debug($"Got {newJobs.Count} new Jobs!");
                }

                foreach (var job in newJobs)
                {
                    if(job.Entry.Name.Equals("ChatSendMessage"))
                    {
                        await ProcessChatSendMessages(job.Entry);
                    }
                    if (job.Entry.Name.Equals("ChatConfirmMessage"))
                    {
                        await ProcessChatConfirmMessages(job.Entry);
                        await SendJobStartMessageAsync();
                    }

                    _knownIds.Add(job.Entry.Id);
                }
            }
            catch (Exception e)
            {
                Logger.Error("Exception occured: ", e);
            }
        }

        public async Task SendJobStartMessageAsync()
        {
            var msg = new MessageReply
            {
                Id = Guid.NewGuid(),
                Priority = MessagePriority.Normal,
                CreatedBy = DeviceId.FullId,
                CreatedAt = DateTimeOffset.Now,
                Type = "JobReply",
                Action = "StartJob",
                Name = "ChatSendMessage"
            };

            await Client.PutActionsMessageAsync(DeviceId, msg);
            Logger.Debug($"Started new Chat as {DeviceId.FullId} with Guid: {msg.Id}");
            ++JobsStarted;
        }

        private Task ProcessChatConfirmMessages(Job job)
        {
            var answer = new Job
            {
                Id = Guid.NewGuid(),
                ReferenceId = job.Id,
                Type = "Job",
                Name = "ChatConfirmMessage",

                CreatedBy = DeviceId.FullId,
                CreatedAt = DateTimeOffset.Now,
                Priority = MessagePriority.Normal,
                Immediate = true,
                Status = JobStatus.Created,
                Resource = job.Resource,
            };
            answer.Resource["submit"] = "";

            SendingQueue.Enqueue(answer);
            Logger.Debug($"Confirmed Message from {job.CreatedBy} as {DeviceId.FullId} with Guid: {job.Id}");
            ++MessagesConfirmed;
            return Task.CompletedTask;
        }

        private Task ProcessChatSendMessages(Job job)
        {
            var receiver = TargetDevice.Invoke();
            var text = $"{Ticker?.ElapsedTicks}";

            var answer = new Job
            {
                Id = Guid.NewGuid(),
                ReferenceId = job.Id,
                Type = "Job",
                Name = "ChatSendMessage",

                CreatedBy = DeviceId.FullId,
                CreatedAt = DateTimeOffset.Now,
                Priority = MessagePriority.Normal,
                Immediate = true,
                Status = JobStatus.Created,
                Resource = new Dictionary<string, string>()
                {
                    {"subject", receiver.FullId},
                    {"text", text},
                    {"submit", ""}
                }
            };

            SendingQueue.Enqueue(answer);
            Logger.Debug($"Sending Message from {DeviceId.FullId} to {receiver.FullId} with Guid: {job.Id}");
            ++MessagesSent;
            return Task.CompletedTask;
        }
    }
}