//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;

namespace SmartDevicesGateway.Model.Requests
{
    public class AdvancedFilterRequest
    {
        public bool FilterExcluding { get; set; }

        public string FilterBy { get; set; }

        public string FilterString { get; set; }
    }
}
