//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using Amqp;

namespace SmartDevicesGateway.Processing.AmqpParser.Base
{
    public interface IMessageParser<out T>
    {
        bool IsResponsible(Message message);

        T ParseMessage(Message message);
    }
}
