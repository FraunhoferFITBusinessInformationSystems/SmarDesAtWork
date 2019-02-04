//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
namespace SmartDevicesGateway.Processing.AmqpParser.Base
{
    public interface IFactory<out T>
    {
        T Create();
    }
}