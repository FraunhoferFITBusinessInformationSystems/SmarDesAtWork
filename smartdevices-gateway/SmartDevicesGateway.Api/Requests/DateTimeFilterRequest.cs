//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Mvc;

namespace SmartDevicesGateway.Api.Requests
{
    public class DateTimeFilterRequest
    {
        [FromQuery]
        public DateTimeOffset From { get; set; }

        [FromQuery]
        public DateTimeOffset To { get; set; }
    }
}
