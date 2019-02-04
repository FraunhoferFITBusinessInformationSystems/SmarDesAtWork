//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Net.Http;
using System.Reflection;
using System.Threading.Tasks;
using log4net;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Messages;
using SmartDevicesGateway.Model.Messages.Enums;
using SmartDevicesGateway.TestCommon;

namespace SmartDevicesGateway.LoadTest.SimpleChat
{
    public class SimpleChatDeviceClient
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        public DeviceId DeviceId { get; }
        public MessageAnswerClient Receiver { get; private set; }
        public MessageSendingQueue Sender { get; private set; }




        public SdgwEndpointClient Client { get; }

        public int JobsStarted { get; private set; } = 0;

        public SimpleChatDeviceClient(Uri baseAddress, DeviceId deviceId)
        {
            DeviceId = deviceId;

            var client = new HttpClient { BaseAddress = baseAddress };
            Client = new SdgwEndpointClient { Client = client };
        }
        
        public void Start()
        {
            Receiver = new MessageAnswerClient(Client);

            Sender.StartSending();
            Receiver.StartPollingForMessages();
        }

        public void Stop()
        {
            Receiver.StopPollingForMessages();
            Sender.StopSending();
        }

        public async Task SendJobStartMessageAsync()
        {
            var msg = new MessageReply
            {
                Id = new Guid(),
                Priority = MessagePriority.Normal,
                CreatedBy = DeviceId.FullId,
                CreatedAt = DateTimeOffset.Now,
                Type = "reply",
                Action = "StartJob",
                AdditionalProperties = { ["jobKey"] = "ChatSendMessage" },
            };

            await Client.PutActionsMessageAsync(DeviceId, msg);
            ++JobsStarted;
        }

        private Task _actionTask;
        private bool _actionSending = false;

        public void StartActions(int timeout)
        {
            _actionTask = new Task(async () => await SendActionAsync(timeout));
            _actionTask.Start();
        }

        private async Task SendActionAsync(int timeout)
        {
            _actionSending = true;
            while (_actionSending)
            {
                await SendJobStartMessageAsync();
                await Task.Delay(timeout);
            }
        }

        public void StopActions()
        {
            _actionSending = false;
        }
    }
}