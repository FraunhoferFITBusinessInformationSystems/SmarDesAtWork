//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.ComponentModel.DataAnnotations;

namespace SmartDevicesGateway.Api.Requests
{
    public class FilterRequest
    {
        public bool FilterExcluding { get; set; }

        public string FilterBy { get; set; }

        public string FilterString { get; set; }

        public bool FilterStateExcluding { get; set; }

        public string FilterStateBy { get; set; }
    }
}
