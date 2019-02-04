//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using System.Reflection;
using System.Text;
using System.Threading;
using log4net;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.TestCommon;

namespace SmartDevicesGateway.LoadTest.SimpleChat
{
    public class AutomaticChatClient
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        public Uri BaseAddress { get; }
        public SdgwEndpointClient Client { get; }
        public Stopwatch Timer { get; }

        public DeviceId[] Targets { get; set; }
        public TimeSpan PollingDelay { get; set; }
        public DeviceId DeviceId { get; set; }

        private readonly Random _random = new Random((int)DateTimeOffset.UtcNow.Ticks);
        public Func<DeviceId> RandomDevice => () => Targets[_random.Next(Targets.Length)];

        public int JobsStarted => _answerer.JobsStarted;
        public int MessagesConfirmed => _answerer.MessagesConfirmed;
        public int MessagesSent => _answerer.MessagesSent;
        public int SendingCount => _answerer.SendingCount;

        private MessageAnswerClient _answerer;

        public AutomaticChatClient(Uri baseAddress)
        {
            BaseAddress = baseAddress;
            var client = new HttpClient { BaseAddress = baseAddress };
            Client = new SdgwEndpointClient { Client = client };

            Timer = Stopwatch.StartNew();
        }

        public void Start()
        {
            Timer.Restart();
            _answerer = new MessageAnswerClient(Client)
            {
                DeviceId = DeviceId,
                PollingDelay = PollingDelay,
                Ticker = Timer,
                TargetDevice = RandomDevice
            };
            _answerer.StartSending();
            _answerer.StartPollingForMessages();
        }

        public void Stop()
        {
            _answerer.StopPollingForMessages();
            _answerer.StopSending();
        }
    }
}
