//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Ui;
using SmartDevicesGateway.Model.Values;

namespace SmartDevicesGateway.Model.Config.ValueSpecifications
{
    public class ConfigValueSpecification
    {
        public string Name { get; set; }
        public ValueDataSource DataSource { get; set; }
    }
}
