//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using Amqp;
using Newtonsoft.Json.Linq;

namespace Vogler.Amqp
{
    public interface IAmqpService
    {
        AmqpListener GetAmqpListener(AmqpListener.ReceiveMessageDelegate messageReceiver, string queueName = null);

        void SendMessage(string queue, string subject, object body, string correlationId = null);

        JObject RequestReply(object message, string subject, string queue, TimeSpan? timeout = null, bool emptyResult = false);
    }
}