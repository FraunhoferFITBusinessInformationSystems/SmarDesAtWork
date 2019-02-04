//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using SmartDevicesGateway.Model.Config.ValueSpecifications;
using SmartDevicesGateway.Model.Values;

namespace SmartDevicesGateway.Model.Ui
{
    public class UiValueSpecification
    {
        public string Name { get; set; }

        public ValueDataSource DataSource { get; set; }

        public UiComponent Component { get; set; }

    }
}
