//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
namespace Vogler.Smartdevices.Common.Amqp
{
    public class AmqpApiResponseError
    {
        public string Module { get; set; }
        public string ErrorCode { get; set; }
        public string ErrorText { get; set; }
    }
}
