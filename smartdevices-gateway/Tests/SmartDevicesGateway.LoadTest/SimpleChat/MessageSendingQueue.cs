//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Concurrent;
using System.Net;
using System.Reflection;
using System.Threading.Tasks;
using log4net;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.TestCommon;

namespace SmartDevicesGateway.LoadTest.SimpleChat
{
    public class MessageSendingQueue
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        public DeviceId DeviceId { get; set; }
        public int SendingCount { get; private set; } = 0;
        public ConcurrentQueue<Job> SendingQueue { get; }
        protected SdgwEndpointClient Client { get; }

        private bool _sendingActive = false;
        private Task _sendingTask;
        
        public MessageSendingQueue(SdgwEndpointClient client)
        {
            Client = client;
            SendingQueue = new ConcurrentQueue<Job>();
        }

        public void StartSending()
        {
            _sendingTask = new Task(() =>
            {
                _sendingActive = true;
                while (_sendingActive)
                {
                    var res = Send();
                    res.Wait();
                    Task.Delay(TimeSpan.FromMilliseconds(2)).Wait();
                }
            });
            _sendingTask.Start();
        }

        public async Task Send()
        {
            while (SendingQueue.Count > 0)
            {
                if (SendingQueue.TryDequeue(out var job))
                {
                    var apiResponse = await Client.PutActionsJobAsync(DeviceId, job);
                    if (apiResponse.HasError || apiResponse.StatusCode != HttpStatusCode.OK)
                    {
                        Logger.Error(
                            $"Error encountered on 'SendJob'. Status Code: {apiResponse.StatusCode.ToString()}",
                            apiResponse.Error);
                    }
                    else
                    {
                        ++SendingCount;
                    }
                }
            }
        }

        public void StopSending()
        {
            _sendingActive = false;
            _sendingTask.Wait(TimeSpan.FromMilliseconds(500));
            _sendingTask.Dispose();
        }
    }
}