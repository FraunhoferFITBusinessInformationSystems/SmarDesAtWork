//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Text;
using System.Threading;
using Amqp;
using Amqp.Framing;
using Amqp.Types;
using log4net;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Vogler.Smartdevices.Common.Amqp;

namespace Vogler.Amqp
{
    public class Cache<TKey, TValue>
    {
        private readonly int _MaxCount;

        private Dictionary<TKey, TValue> items = new Dictionary<TKey, TValue>();

        public Cache(int maxCount)
        {
            _MaxCount = maxCount;
        }

        public TValue Get(TKey key)
        {
            return items[key];
        }

        public void Add(TKey key, TValue value)
        {
        }
    }

    public class AmqpService : IAmqpService
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
        protected readonly Dictionary<string, AmqpConnection> _connections;
        protected Connection _amqpConnection;
        protected readonly string _connectionString;
        protected readonly string _clientName;
        private Session _session;

        public AmqpService(AmqpServiceConfig amqpConfig)
        {
            _connectionString = amqpConfig.ConnectionString;
            _clientName = amqpConfig.ClientName;

            _connections = new Dictionary<string, AmqpConnection>();

            Connect();
        }

        protected virtual void Connect()
        {
            if (_amqpConnection != null && !_amqpConnection.IsClosed)
            {
                return;
            }

            try
            {
                var address = new Address(_connectionString);
                Logger.Debug($"Establishing AmqpConnection {address.Scheme}{address.Host}:{address.Port}...");
                _amqpConnection = new Connection(address);
                _session = new Session(_amqpConnection);
                ;
            }
            catch (IOException e)
            {
                Logger.Error(e);

                throw new AmqpConnectionException("Exception while AMQP connection buildup.", e);
            }
        }


        public virtual AmqpListener GetAmqpListener(AmqpListener.ReceiveMessageDelegate messageReceiver,
            string queueName = null)
        {
            if (queueName == null)
            {
                queueName = Guid.NewGuid().ToString();
            }

            return new AmqpListener(_connectionString, _clientName, queueName, messageReceiver);
        }


        public virtual void SendMessage(string queue, string subject, object body, string correlationId = null)
        {
            var serializedContent = JsonConvert.SerializeObject(body);

            if (serializedContent == null)
            {
                throw new ArgumentNullException(nameof(serializedContent));
            }

            if (queue == null)
            {
                throw new ArgumentNullException(nameof(queue));
            }

            var bodySection = new Data {Binary = Encoding.UTF8.GetBytes(serializedContent)};

            Logger.Debug($"Sending [{subject}] -> [{queue}]: {serializedContent}");

            GetConnection(queue).SendMessage(CreateMessage(subject, bodySection, correlationId));
        }


        private static Message CreateMessage(string subject, Data bodySection, string correlationId)
        {
            if (bodySection == null)
            {
                throw new ArgumentNullException(nameof(bodySection));
            }

            var message = new Message
            {
                Properties = new Properties
                {
                    Subject = subject,
                    CorrelationId = correlationId
                },
                BodySection = bodySection
            };

            return message;
        }

        public virtual JObject RequestReply(object message, string subject, string queue, TimeSpan? timeout = null, bool emptyResult = false)
        {
            JObject res = null;

            if (timeout == null)
            {
                timeout = TimeSpan.FromMilliseconds(3000);
            }

            var session = new Session(_amqpConnection);
            var sender = new SenderLink(session, $"Sender-{Guid.NewGuid().ToString()}", queue);

            var replyTo = "";
            var receiverAttached = new ManualResetEvent(false);

            void OnReceiverAttached(ILink l, Attach a)
            {
                replyTo = ((Source) a.Source).Address;
                receiverAttached.Set();
            }

            var source = new Source
            {
                Dynamic = false,
                Address = Guid.NewGuid().ToString(),
                Capabilities = new Symbol[] { new Symbol("topic") }
            };

            var receiver = new ReceiverLink(
                session, $"Receiver-{Guid.NewGuid().ToString()}", source, OnReceiverAttached);

            if (receiverAttached.WaitOne(timeout.Value))
            {
                var request = new Message(AmqpUtils.SerializeMessage(message))
                {
                    Properties = new Properties
                    {
                        Subject = subject,
                        ReplyTo = replyTo
                    }
                };

                sender.Send(request);
                sender.Close();

                var response = receiver.Receive(timeout.Value);

                if (null != response)
                {
                    receiver.Accept(response);

                    res = AmqpUtils.DeserializeMessage(response.Body);
                }

                receiver.Close();
                session.Close();

                if (res != null)
                {
                    return res;
                }

                if (emptyResult)
                {
                    return null;
                }

                throw new AmqpApiException("AMQP: Receiver timeout receiving response");
            }

            session.Close();

            throw new AmqpApiException("AMQP: Receiver attach timeout");
        }

    protected virtual AmqpConnection GetConnection(string queueName)
        {
            if (_connections.Count > 200)
            {
                foreach (var value in _connections.Values)
                {
                    value.Dispose();
                }

                _connections.Clear();
            }

            if (_connections.ContainsKey(queueName))
            {
                var amqpConnection = _connections[queueName];

                if (amqpConnection.IsOk())
                {
                    return amqpConnection;
                }

                _connections.Remove(queueName);
            }

            _connections[queueName] = new AmqpConnection(_amqpConnection, queueName, _clientName);

            return _connections[queueName];
        }

        protected virtual Session GetSession()
        {
            var sessionAttached = new ManualResetEvent(false);
            void OnSessionAttached(ISession s, Begin b)
            {
                sessionAttached.Set();
            }

            var begin = new Begin()
            {
                IncomingWindow = 2048,
                OutgoingWindow = 2048,
                HandleMax = (uint)(64 - 1),
                NextOutgoingId = uint.MaxValue - 2u
            };

            var session = new Session(_amqpConnection, begin, OnSessionAttached);

            if (sessionAttached.WaitOne(5000))
            {
                return session;
            }
            throw new Exception("AMQP: Session attach timeout");
        }
    }
}