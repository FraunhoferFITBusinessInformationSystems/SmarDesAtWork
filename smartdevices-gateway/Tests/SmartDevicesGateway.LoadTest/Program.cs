//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Diagnostics.CodeAnalysis;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using Amqp;
using log4net;
using log4net.Config;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Serialization;
using SmartDevicesGateway.Common.Extensions;
using SmartDevicesGateway.LoadTest.SimpleChat;
using SmartDevicesGateway.Model.Dto.Config;

[assembly: XmlConfigurator(ConfigFile = "log4net.config", Watch = true)]

namespace SmartDevicesGateway.LoadTest
{
    class Program
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        static void Main(string[] args)
        {
            var logRepository = LogManager.GetRepository(Assembly.GetEntryAssembly());
            XmlConfigurator.Configure(logRepository, new FileInfo("log4net.config"));
            GlobalContext.Properties["LogPath"] = Common.Logging.LoggingHelper.GetDefaultLogPathBySystem("SmartDevicesLoadTest", "");
            GlobalContext.Properties["LogPathDebug"] = Common.Logging.LoggingHelper.GetDefaultLogPathBySystem("SmartDevicesLoadTest", "debug");

            JsonConvert.DefaultSettings = () => new JsonSerializerSettings()
            {
                ContractResolver = new CamelCasePropertyNamesContractResolver(),
                NullValueHandling = NullValueHandling.Ignore,
                ReferenceLoopHandling = ReferenceLoopHandling.Ignore,
                DateTimeZoneHandling = DateTimeZoneHandling.Utc,
                Converters = new JsonConverter[]
                {
                    new IsoDateTimeConverter
                    {
                        DateTimeFormat = "yyyy'-'MM'-'dd'T'HH':'mm':'ss.fffzzz"
                        //DateTimeFormat = "yyyy'-'MM'-'dd'T'HH':'mm':'ss"
                    }
                },
            };
            
            StartLoadTest(args);
//            StartSimpleTest(args);
        }

        /// <summary>
        /// Starts a simulation with only one participant.
        /// The Devices is sending chat-messages to itself and confirms them.
        /// For every confirmed Message it starts a new one.
        /// </summary>
        /// <param name="args">Programm arguments</param>
        private static void StartSimpleTest(string[] args)
        {
            var client = new AutomaticChatClient(new Uri("http://localhost:7000/"))
            {
                DeviceId = new DeviceId("debug1.A"),
                Targets = new[] {new DeviceId("debug1.A")},
                PollingDelay = TimeSpan.FromMilliseconds(10),
            };
            client.Start();

            var running = true;
            while (running)
            {
                if (Console.KeyAvailable)
                {
                    var consoleKeyInfo = Console.ReadKey();

                    if (consoleKeyInfo.Key == ConsoleKey.C &&
                        (consoleKeyInfo.Modifiers & ConsoleModifiers.Control) != 0)
                    {
                        running = false;
                    }
                }
            }
            client.Stop();
        }


        static void StartLoadTest(string[] args)
        {
            const int clientCount = 8;
            var uri = new Uri("http://localhost:7000/");
            var delay = TimeSpan.FromMilliseconds(10);
            var devices = clientCount.Repeat(i => new DeviceId($"debug{i + 1}.A")).ToArray();
            var clients = clientCount.Repeat(i => new AutomaticChatClient(uri)
            {
                DeviceId = devices[i],
                Targets = devices,
                PollingDelay = delay
            }).ToArray();

            var statistics = new ChatClientStatistics(clients);
            
            Logger.Info("Starting ChatLoadTest!");

            clientCount.Repeat(i =>
            {
                clients[i].Start();
            });
            statistics.StartMeasurement();

            Logger.Info("Test running.");
            
            var running = true;
            var startTime = DateTimeOffset.Now;
            var b = false;

            while (running)
            {
                if (Console.KeyAvailable)
                {
                    var consoleKeyInfo = Console.ReadKey();

                    if (consoleKeyInfo.Key == ConsoleKey.C &&
                        (consoleKeyInfo.Modifiers & ConsoleModifiers.Control) != 0)
                    {
                        running = false;

                        continue;
                    }
                }

                var span = DateTimeOffset.Now - startTime;

                if (span.Seconds % 10 == 0)
                {
                    if (!b)
                    {
                        Logger.Info(statistics.PrintStatus());
                        b = true;
                    }
                }
                else
                {
                    b = false;
                }
            }

            statistics.PrintStatus();
            clientCount.Repeat(i =>
            {
                clients[i].Stop();
            });
            Console.WriteLine("ChatLoadTest stopped!");
            
        }


    }

    public class ChatClientStatistics
    {
        private readonly AutomaticChatClient[] _clients;

        public DateTime StartTime { get; set; }
        public Stopwatch Timer { get; set; }

        public ChatClientStatistics(AutomaticChatClient[] clients)
        {
            this._clients = clients;
        }

        public void StartMeasurement()
        {
            StartTime = DateTime.Now;
            Timer = Stopwatch.StartNew();
        }

        public string PrintStatus()
        {
            var e = Timer.Elapsed;
            var totalMs = e.TotalMilliseconds;

            var sb = new StringBuilder();
            sb.Append("------------------------------\n");
            sb.Append("  Status Printout:\n");

            sb.Append("  Runtime:            ").AppendFormat("{0},{1}s\n", e.TotalSeconds, e.Milliseconds);

            var startedMessages = SumSelectedInt(_clients, x => x.JobsStarted);
            sb.Append("  Actions initiated:  ").Append(AggregateStrings(_clients, x => x.JobsStarted.ToString()));
            sb.AppendFormat(" Sum: {0}\n", startedMessages);

            var sentMessages = SumSelectedInt(_clients, x => x.MessagesSent);
            sb.Append("  Messages sent:      ").Append(AggregateStrings(_clients, x => x.MessagesSent.ToString()));
            sb.AppendFormat(" Sum: {0}\n", sentMessages);

            var confirmedMessages = SumSelectedInt(_clients, x => x.MessagesConfirmed);
            sb.Append("  Messages confirmed: ").Append(AggregateStrings(_clients, x => x.MessagesConfirmed.ToString()));
            sb.AppendFormat(" Sum: {0}\n", confirmedMessages);

            var outgoingMessages = SumSelectedInt(_clients, x => x.SendingCount);
            sb.Append("  Outgoing Msg:       ").Append(AggregateStrings(_clients, x => x.SendingCount.ToString()));
            sb.AppendFormat(" Sum: {0}\n", outgoingMessages);

            sb.Append("------------------------------\n");

            return sb.ToString();
        }

        [SuppressMessage("ReSharper", "EnforceIfStatementBraces")]
        private string AggregateStrings<T>(IEnumerable<T> tList, Func<T, string> selector)
        {
            var sb = new StringBuilder();
            sb.Append("[ ");
            var first = true;
            foreach (var t in tList)
            {
                if (first) first = false;
                else sb.Append(", ");
                sb.Append("( ").Append(selector.Invoke(t)).Append(" )");
            }
            sb.Append(" ]");
            return sb.ToString();
        }

        private int SumSelectedInt<T>(IEnumerable<T> tList, Func<T, int> selector)
        {
            return tList.Sum(selector.Invoke);
        }
    }
}