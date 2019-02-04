//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Ui;

namespace SmartDevicesGateway.Model.Dto.Data
{
    public class JobEntryDto : TabEntryDto
    {
        public UiLayout Ui { get; set; }
    }
}
