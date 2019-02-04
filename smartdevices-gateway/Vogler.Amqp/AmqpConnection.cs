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
using Message = Amqp.Message;

namespace Vogler.Amqp
{
    public class AmqpConnection : IDisposable
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
        private readonly Connection _amqpConnection;
        private readonly string _queueName;
        private readonly string _clientName;

        private ReceiverLink _receiverLink;
        private SenderLink _senderLink;
        private Session _session;

        //TODO add mutltiple listeners?
        public delegate bool ReceiveMessageDelegate(Message message);

        public ReceiveMessageDelegate MessageReceiver { get; set; } = null;

        protected AmqpConnection()
        {
        }

        internal AmqpConnection(Connection amqpConnection, string queueName, string clientName)
        {
            _amqpConnection = amqpConnection;
            _queueName = queueName;
            _clientName = clientName;

            _session = new Session(_amqpConnection);
        }

        public bool IsOk()
        {
            return
                _amqpConnection != null &&
                !_amqpConnection.IsClosed &&
                _session != null &&
                !_session.IsClosed;
        }

        /// <summary>
        /// Starts the Listener on the Specified Queue. Received Messages are delivered to the
        /// <see cref="MessageReceiver"/>-Delegate.
        /// </summary>
        public virtual void StartListening()
        {
            if (_receiverLink == null || _receiverLink.IsClosed)
            {
                var source = new Source
                {
                    Address = _queueName,
                    Capabilities = new Symbol[] {new Symbol("topic")}
                    //Register Queue as Multicast-Queue (topic)
                };
                _receiverLink = new ReceiverLink(_session, $"{_clientName}-receiver", source, null);
            }

            _receiverLink.Start(1, OnMessage);
        }

        public virtual void StopListening(TimeSpan timeout = default(TimeSpan), Error error = null)
        {
            if (_receiverLink == null || _receiverLink.IsClosed)
            {
                return;
            }

            if (timeout == default(TimeSpan))
            {
                _receiverLink.Close();
            }
            else
            {
                _receiverLink.Close(timeout, error);
            }
        }

        private void OnMessage(IReceiverLink receiverLink, Message message)
        {
            Logger.Debug($"Message Received: Queue {_queueName}\n{message?.Body}");

            try
            {
                if (MessageReceiver == null)
                {
                    return;
                }

                //var props = message.ApplicationProperties?.Map;

                //if (props == null)
                //{
                //    _receiverLink?.Accept(message);
                //    return;
                //}
                var ret = false;

                try
                {
                    ret = MessageReceiver.Invoke(message);
                }
                catch (Exception e)
                {
                    Logger.Error("Exception in onMessageReceived!", e);
                }

                if (ret)
                {
                    Logger.Debug("Message has been accepted.");
                    _receiverLink?.Accept(message);
                }
            }
            catch (Exception ex)
            {
                Logger.Error(ex.Message);
            }
        }

        public virtual void SendMessage(Message message)
        {
            if (_senderLink == null || _senderLink.IsClosed || _senderLink.Session.IsClosed)
            {
                if (_senderLink != null)
                {
                    _senderLink.Detach();
                    _senderLink.Close();
                }

                _senderLink = new SenderLink(_session, _clientName + "-sender", _queueName);
            }

            _senderLink.Send(message);
        }

        public void Dispose()
        {
            if (_receiverLink != null && !_receiverLink.IsClosed)
            {
                _receiverLink?.Close();
            }

            if (_senderLink != null && !_senderLink.IsClosed)
            {
                _senderLink?.Close();
            }

            if (_session != null && !_session.IsClosed)
            {
                _session?.Close();
            }
        }
    }
}