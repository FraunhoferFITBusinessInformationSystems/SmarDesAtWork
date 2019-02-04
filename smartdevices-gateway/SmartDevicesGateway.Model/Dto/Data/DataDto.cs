//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Messages.Base;
using SmartDevicesGateway.Model.Values;

namespace SmartDevicesGateway.Model.Dto.Data
{
    public class DataDto : ValueDataDto
    {
        public IEnumerable<Job> Jobs { get; set; }

        public IEnumerable<IMessage> Messages { get; set; }
    }
}
