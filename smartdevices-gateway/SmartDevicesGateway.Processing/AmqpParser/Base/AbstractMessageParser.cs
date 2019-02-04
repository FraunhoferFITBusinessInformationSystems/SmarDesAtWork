//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using Amqp;

namespace SmartDevicesGateway.Processing.AmqpParser.Base
{
    public abstract class AbstractMessageParser<T> : IMessageParser<T>
    {
        public IFactory<T> Factory { get; }

        protected AbstractMessageParser()
        {
            Factory = null;
        }

        protected AbstractMessageParser(IFactory<T> factory)
        {
            Factory = factory;
        }

        public abstract bool IsResponsible(Message message);
        public abstract T ParseMessage(Message message);
    }
}
