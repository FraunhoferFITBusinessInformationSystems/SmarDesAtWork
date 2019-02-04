//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics.CodeAnalysis;
using System.Diagnostics.Tracing;
using System.IO;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Amqp;
using Amqp.Framing;
using Amqp.Types;
using log4net;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using SmartDevicesGateway.Processing.Controller.Util;
using SmartDevicesGateway.TestCommon;
using SmartDevicesGateway.UnitTests;
using SmartDevicesGateway.UnitTests.Fixtures;
using Vogler.Amqp;
using Xunit;
using Trace = System.Diagnostics.Trace;

namespace SmartDevicesGateway.IntegrationTests.ruleengine
{
    [Trait("Requires", Traits.BROKER)]
    [Trait("Category", Categories.INTEGRATION)]
    public class DynamicAmqpRequestTest : IClassFixture<AmqpFixture>
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
        public AmqpFixture Fixture { get; }

        public const string Domain = "TodoSample";

        public DynamicAmqpRequestTest(AmqpFixture fixture)
        {
            Fixture = fixture;
            Fixture.LoggerFactory.CreateLogger<ConfigServiceTest>();
        }

        //        [Fact]
        //        public async Task SendMessage20MB()
        //        {
        //
        //            var conn = Fixture.AmqpService.GetConnection("Resources");
        //
        //            int sizeKB = 1024 * 20;
        //
        //            //generate large payload
        //            var rand = new Random(System.DateTimeOffset.Now.Millisecond);
        //            var ms = new MemoryStream();
        //            var buffer = new byte[1024];
        //            for (var written = 0L; written < sizeKB; written++)
        //            {
        //                rand.NextBytes(buffer);
        //                ms.Write(buffer, 0, buffer.Length);
        //            }
        //            var base64String = Convert.ToBase64String(ms.ToArray());
        //
        //            var msg = new
        //            {
        //                name = $"rand{sizeKB}KB.bin",
        //                mimeType = "application/octet-stream",
        //                body = base64String
        //            };
        //            var jsonText = JsonConvert.SerializeObject(msg);
        //            var bytes = Encoding.UTF8.GetBytes(jsonText);
        //
        //            Message request = new Message(bytes);
        //            request.Properties = new Properties()
        //            {
        //                MessageId = "request" + 0,
        //                ReplyTo = "foo",
        //                Subject = "PutResource"
        //            };
        //
        //            conn.SendMessage(request);
        //        }

        /// <summary>
        /// Testing the Interop.Client that follows the official test client sample in the Amqp.Net Library.
        /// </summary>
        /// <returns></returns>
        /// 
        [Fact(Skip = "Deprecated development test")]
        [Trait("Requires", "ruleengine")]
        [Category("integration")]
        public void TestInteropClient()
        {
            
            var requestQueueName = "Resources";

//            Connection.DisableServerCertValidation = true;

            // uncomment the following to write frame traces
//            Amqp.Trace.TraceLevel = TraceLevel.Frame;
//            Amqp.Trace.TraceListener = (l, f, a) =>
//            {
//                switch (l)
//                {
//                    case TraceLevel.Error: Logger.ErrorFormat(f, a); break;
//                    case TraceLevel.Warning: Logger.WarnFormat(f, a); break;
//                    case TraceLevel.Information:
//                    case TraceLevel.Output: Logger.InfoFormat(f, a); break;
//                    case TraceLevel.Verbose:
//                    case TraceLevel.Frame:
//                    default: Logger.DebugFormat(f, a); break;
//                }
//            };

            int sizeKB = 1024 * 10; //10MB

            //generate large payload
            var rand = new Random(System.DateTimeOffset.Now.Millisecond);
            var ms = new MemoryStream();
            var buffer = new byte[1024];
            for (var written = 0L; written < sizeKB; written++)
            {
                rand.NextBytes(buffer);
                ms.Write(buffer, 0, buffer.Length);
            }
            var base64String = Convert.ToBase64String(ms.ToArray());

            var msg = new
            {
                name = $"rand{sizeKB}KB.bin",
                mimeType = "application/octet-stream",
                body = base64String
            };
            var jsonText = JsonConvert.SerializeObject(msg);
            var bytes = Encoding.UTF8.GetBytes(jsonText);

            
            var amqpService = Fixture.AmqpService;
//            try
//            {
            var session = amqpService.GetSession();

            // Sender attaches to fixed request queue name
            var sender = new SenderLink(session, "Interop.Client-sender", requestQueueName);

            // Receiver attaches to dynamic address.
            // Discover its name when it attaches.
            var replyTo = "";
            var receiverAttached = new ManualResetEvent(false);

            void OnReceiverAttached(ILink l, Attach a)
            {
                replyTo = ((Source)a.Source).Address;
                receiverAttached.Set();
            }

            // Create receiver and wait for it to attach.
            var receiver = new ReceiverLink(
                session, "Interop.Client-receiver", new Source() { Dynamic = true }, OnReceiverAttached);
            if (receiverAttached.WaitOne(10000))
            {
                // Receiver is attached.
                Logger.Debug("Receiver is attached");

                var request = new Message(bytes)
                {
                    Properties = new Properties()
                    {
                        MessageId = "request" + 0,
                        ReplyTo = replyTo,
                        Subject = "PutResource"
                    }
                };

                sender.Send(request);
                var response = receiver.Receive();
                if (null != response)
                {
                    receiver.Accept(response);
                    var message = AmqpUtils.DeserializeMessage(response?.Body);
                    var uuid = message["responseObject"]?["uuid"]?.ToString();
                    Assert.NotNull(uuid);
                    var guid = Guid.Parse(uuid);
                }
                else
                {
                    throw new ApplicationException(
                        string.Format("Receiver timeout receiving response {0}", 0));
                }
            }

            else
            {
                throw new ApplicationException("Receiver attach timeout");
            }
            receiver.Close();
            sender.Close();
            session.Close();
            return;
//            }
//            catch (Exception e)
//            {
//                Console.Error.WriteLine("Exception {0}.", e);
//                if (null != connection)
//                {
//                    connection.Close();
//                }
//            }
        }

        [Fact(Skip = "Deprecated development test")]
        [Trait("Requires", "ruleengine")]
        [Category("integration")]
        public void TestSendInteropClientResourceRequest()
        {
            int sizeKB = 1024 * 10; //10MB

            //generate large payload
            var rand = new Random(System.DateTimeOffset.Now.Millisecond);
            var ms = new MemoryStream();
            var buffer = new byte[1024];
            for (var written = 0L; written < sizeKB; written++)
            {
                rand.NextBytes(buffer);
                ms.Write(buffer, 0, buffer.Length);
            }
            var base64String = Convert.ToBase64String(ms.ToArray());

            var msg = new
            {
                name = $"rand{sizeKB}KB.bin",
                mimeType = "application/octet-stream",
                body = base64String
            };

            var response = SendInteropClientRequest(msg, "PutResource", "Resources");
            Assert.NotNull(response);
            var uuid = response["responseObject"]?["uuid"]?.ToString();
            Assert.NotNull(uuid);
            var guid = Guid.Parse(uuid);

        }

        [Fact(Skip = "Deprecated development test")]
        [Trait("Requires", "ruleengine")]
        [Category("integration")]
        public void TestSendInteropTodoRequest()
        {
            var msg = new
            {
                context = new { },
                domain = Domain
            };
            
            var response = SendInteropClientRequest(msg, "ToDoFind", "ToDo");
            Assert.NotNull(response);
//            var uuid = response["responseObject"]?["uuid"]?.ToString();
//            Assert.NotNull(uuid);
//            var guid = Guid.Parse(uuid);

        }

        public JObject SendInteropClientRequest(object msg, string subject, string requestQueue)
        {
            JObject message = null;

            var bytes = AmqpUtils.SerializeMessage(msg);

            var amqpService = Fixture.AmqpService;
            var session = amqpService.GetSession();
            SenderLink sender = null;
            ReceiverLink receiver = null;

            try
            {
                

                // Sender attaches to fixed request queue name
                sender = new SenderLink(session, "Interop.Client-sender", requestQueue);

                // Receiver attaches to dynamic address.
                // Discover its name when it attaches.
                var replyTo = "";
                var receiverAttached = new ManualResetEvent(false);

                void OnReceiverAttached(ILink l, Attach a)
                {
                    replyTo = ((Source) a.Source).Address;
                    receiverAttached.Set();
                }

                // Create receiver and wait for it to attach.
                receiver = new ReceiverLink(
                    session, "Interop.Client-receiver", new Source() {Dynamic = true}, OnReceiverAttached);

                if (receiverAttached.WaitOne(10000))
                {
                    // Receiver is attached.
                    Logger.Debug("Receiver is attached");

                    var request = new Message(bytes)
                    {
                        Properties = new Properties()
                        {
//                            MessageId = "request" + 0,
                            CorrelationId = replyTo,
                            ReplyTo = replyTo,
                            Subject = subject
                        }
                    };

                    sender.Send(request);
                    var response = receiver.Receive(TimeSpan.FromSeconds(8));

                    if (null != response)
                    {
                        receiver.Accept(response);
                        message = AmqpUtils.DeserializeMessage(response?.Body);
                    }
                    else
                    {
                        throw new ApplicationException(
                            string.Format("Receiver timeout receiving response {0}", 0));
                    }
                }
                else
                {
                    throw new ApplicationException("Receiver attach timeout");
                }
            }
            catch (Exception e)
            {
                throw new ApplicationException("Exception: ", e);
            }
            finally
            {
                receiver?.Close();
                sender?.Close();
                session.Close();
            }
            return message;
        }

        [Fact(Skip = "Deprecated development test")]
        [SuppressMessage("ReSharper", "InconsistentNaming")]
        public void TestDynamicRequestManually()
        {
            var Namespace = "test";
            var CorrelationId = "42";
            var ServiceName = "integrationtest";
            var Timeout = TimeSpan.FromSeconds(10);
            var LinkId = Guid.NewGuid();

            var obj = new
            {
                context = new { },
                domain = Domain
            };
            var jsonText = JsonConvert.SerializeObject(obj);
            var bytes = Encoding.UTF8.GetBytes(jsonText);
            const string requestQueue = "ToDo";
            const string subject = "ToDoFind";

            // AmqpDynamicRequest:
            var responseQueue = $"{Namespace}.{ServiceName}.{LinkId}";
            var source = new Source
            {
                Address = responseQueue,
                Capabilities = new Symbol[] { new Symbol("topic") } //TODO why topic? This should work with queue! Presumably it's the Api's fault.    
            };
            var _senderSession = Fixture.AmqpService.GetSession();
            var _receiverSession = Fixture.AmqpService.GetSession();
            var _sender = new SenderLink(_senderSession, $"dyn-sender-{LinkId}", requestQueue);
            var _receiver = new ReceiverLink(_receiverSession, $"dyn-receiver-{LinkId}", source, null);

            Message response = null;
            var sem = new SemaphoreSlim(0);

            System.Diagnostics.Trace.WriteLine(string.Format("T{0}: Creating second Thread", Thread.CurrentThread.ManagedThreadId));
            var thread = new Thread(() =>
            {
                try
                {
                    System.Diagnostics.Trace.WriteLine(string.Format("T{0}-Receiver: Start Listening", Thread.CurrentThread.ManagedThreadId));
                    var rmsg = _receiver.Receive(Timeout);
                    System.Diagnostics.Trace.WriteLine(string.Format("T{0}-Receiver: Received something: {1}", Thread.CurrentThread.ManagedThreadId, rmsg));

                    if (rmsg != null)
                    {
                        //no Timeout occurred
                        _receiver.Accept(rmsg);
                        //                        response = DeserializeMessage(rmsg.Body);
                        response = rmsg;
                    }
                    sem.Release();
                }
                catch (Exception e)
                {
                    Logger.Warn("Error in AmqpDynamicRequestListener!", e);
                }
            });
            thread.Start();

            //send request
            var msg = new Message(bytes)
            {
                Properties = new Properties
                {
                    ReplyTo = responseQueue,
                    Subject = subject,
                    CorrelationId = CorrelationId,
                }
            };
            System.Diagnostics.Trace.WriteLine(string.Format("T{0}-Sender: Sending message", Thread.CurrentThread.ManagedThreadId));
            _sender.Send(msg);

            System.Diagnostics.Trace.WriteLine(string.Format("T{0}-Sender: Wait until receiving message", Thread.CurrentThread.ManagedThreadId));
            sem.Wait();


            _sender?.Close();
            _receiver?.Close();


            Assert.NotNull(response);
        }

        [Fact(Skip = "Deprecated development test")]
        [SuppressMessage("ReSharper", "InconsistentNaming")]
        public async Task TestDynamicRequestManuallyAsync()
        {
            var Namespace = "test";
            var CorrelationId = "42";
            var ServiceName = "integrationtest";
            var Timeout = TimeSpan.FromSeconds(10);
            var LinkId = Guid.NewGuid();


            var obj = new
            {
                context = new { },
                domain = Domain
            };
            var jsonText = JsonConvert.SerializeObject(obj);
            var bytes = Encoding.UTF8.GetBytes(jsonText);
            var requestQueue = "ToDo";
            var subject = "ToDoFind";

            // AmqpDynamicRequest:
            var responseQueue = $"{Namespace}.{ServiceName}.{LinkId}";
            Source source = new Source
            {
                Address = responseQueue,
                Capabilities = new Symbol[] { new Symbol("topic") } //TODO why topic? This should work with queue! Presumably it's the Api's fault.
            };
            var _senderSession = Fixture.AmqpService.GetSession();
            var _receiverSession = Fixture.AmqpService.GetSession();
            var _sender = new SenderLink(_senderSession, $"dyn-sender-{LinkId}", requestQueue);
            var _receiver = new ReceiverLink(_receiverSession, $"dyn-receiver-{LinkId}", source, null);

            Message response = null;
            SemaphoreSlim sem = new SemaphoreSlim(0);
            System.Diagnostics.Trace.WriteLine(string.Format("T{0}: Creating second Thread", Thread.CurrentThread.ManagedThreadId));
            var thread = new Thread(() =>
            {
                Message rmsg;
                try
                {
                    System.Diagnostics.Trace.WriteLine(string.Format("T{0}-Receiver: Start Listening", Thread.CurrentThread.ManagedThreadId));
                    rmsg = _receiver.Receive(Timeout);
                    System.Diagnostics.Trace.WriteLine(string.Format("T{0}-Receiver: Received something: {1}", Thread.CurrentThread.ManagedThreadId, rmsg));

                    if (rmsg != null)
                    {
                        //no Timeout occurred
                        _receiver.Accept(rmsg);
                        //                        response = DeserializeMessage(rmsg.Body);
                        response = rmsg;
                    }
                    sem.Release();
                }
                catch (Exception e)
                {
                    Logger.Warn("Error in AmqpDynamicRequestListener!", e);
                }
            });
            thread.Start();

            //send request
            var msg = new Message(bytes)
            {
                Properties = new Properties
                {
                    ReplyTo = responseQueue,
                    Subject = subject,
                    CorrelationId = CorrelationId,
                }
            };
            System.Diagnostics.Trace.WriteLine(string.Format("T{0}-Sender: Sending message", Thread.CurrentThread.ManagedThreadId));
            await _sender.SendAsync(msg);

            Trace.WriteLine(string.Format("T{0}-Sender: Wait until receiving message", Thread.CurrentThread.ManagedThreadId));
            await sem.WaitAsync();


            await _sender?.CloseAsync();
            await _receiver?.CloseAsync();


            Assert.NotNull(response);
        }
    }
}
