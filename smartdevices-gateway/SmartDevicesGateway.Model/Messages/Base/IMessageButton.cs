//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Messages.Enums;

namespace SmartDevicesGateway.Model.Messages.Base
{
    public interface IMessageButton
    {
        string Text { get; set; }
        MessageButtonType ButtonType { get; set; }
        string ReplyUrl { get; set; }
    }
}
