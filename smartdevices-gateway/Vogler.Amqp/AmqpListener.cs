//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Reflection;
using Amqp;
using Amqp.Framing;
using Amqp.Types;
using log4net;
using Polly;
using Message = Amqp.Message;

namespace Vogler.Amqp
{
    public class AmqpListener
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
        public delegate bool ReceiveMessageDelegate(Message message);
        private readonly string _ConnectionString;
        private readonly string _ClientName;
        private readonly string _QueueName;
        private readonly ReceiveMessageDelegate _MessageReceiver;
        private Connection _Connection;
        private Session _Session;
        private ReceiverLink _ReceiverLink;

        public string ClientName => _ClientName;
        public string QueueName => _QueueName;

        public AmqpListener(string connectionString, string clientName, string queueName,
            ReceiveMessageDelegate messageReceiver) 
        {
            _ConnectionString = connectionString;
            _ClientName = clientName;
            _QueueName = queueName;
            _MessageReceiver = messageReceiver;

            Connect();
        }

        private void Connect()
        {
            Policy
                .Handle<Exception>()
                .WaitAndRetry(30, i => TimeSpan.FromSeconds(5),
                    (exception, timeSpan) =>
                    {
                        Logger.Warn($"AMQP Exception {exception.Message} - retrying connection in {timeSpan} seconds.");
                    })
                .Execute(() =>
                {
                    _Connection = new Connection(new Address(_ConnectionString));
                    _Session = new Session(_Connection);

                    var source = new Source
                    {
                        Address = _QueueName,
                        Capabilities = new[] { new Symbol("topic") }, //Register Queue as Multicast-Queue (topic)
                    };

                    _ReceiverLink = new ReceiverLink(_Session, $"{_ClientName}-receiver", source, null);

                    _Connection.Closed += OnClosed;
                    _Session.Closed += OnClosed;
                    _ReceiverLink.Closed += OnClosed;

                    _ReceiverLink.Start(1, OnMessage);
                    Logger.Info($"Listener {_ClientName} now listening on AMQP Host: {_ConnectionString} Queue: {_QueueName}");
                });
        }

        public void Disconnect()
        {
            _Connection.Closed -= OnClosed;
            _Session.Closed -= OnClosed;
            _ReceiverLink.Closed -= OnClosed;

            _ReceiverLink.Close();
            _Session.Close();
            _Connection.Close();
        }

        private void OnClosed(IAmqpObject sender, Error error)
        {
            Logger.Warn($"AMQP Connection closed: {error.Description}");

            _Connection.Closed -= OnClosed;
            _Session.Closed -= OnClosed;
            _ReceiverLink.Closed -= OnClosed;

            Connect();
        }

        private void OnMessage(IReceiverLink receiverLink, Message message)
        {
            try
            {
                var ret = false;

                try
                {
                    ret = _MessageReceiver(message);
                }
                catch (Exception e)
                {
                    Logger.Error("Exception in onMessageReceived!", e);
                }

                if (ret)
                {
                    Logger.Debug("Message has been accepted.");
                    receiverLink?.Accept(message);
                }
            }
            catch (Exception ex)
            {
                Logger.Error(ex.Message);
            }
        }
    }
}