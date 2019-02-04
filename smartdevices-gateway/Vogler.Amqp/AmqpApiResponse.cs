//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json.Linq;

namespace Vogler.Smartdevices.Common.Amqp
{
    public class AmqpApiResponse
    {
        public JObject ResponseObject { get; set; }
        public AmqpApiResponseError Error { get; set; }
    }
}
