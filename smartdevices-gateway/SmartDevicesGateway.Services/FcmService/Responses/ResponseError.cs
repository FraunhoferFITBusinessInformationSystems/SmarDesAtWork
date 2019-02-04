//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.ComponentModel;

namespace SmartDevicesGateway.Services.FcmService.Responses
{
    public enum ResponseError
    {
        /// <summary>No error occurred.</summary>
        [Description("No error")]
        NoError = 0,

        [Description("Unknown Error")]
        UnknownError,    

        [Description("Missing Registration Token")]
        MissingRegistration,

        [Description("Unregistered Device")]
        NotRegistered,

        [Description("Invalid Registration Token")]
        InvalidRegistration,
        
        [Description("Invalid Package Name")]
        InvalidPackageName,
        
        [Description("Mismatched Sender")]
        MismatchSenderId,
        
        [Description("FcmMessage Too Big")]
        MessageTooBig,
        
        [Description("Invalid Data Key")]
        InvalidDataKey,
        
        [Description("Invalid Time to Live")]
        InvalidTtl,
        
        [Description("Timeout")]
        Unavailable,
        
        [Description("Internal Server Error")]
        InternalServerError,
        
        [Description("Device FcmMessage Rate Exceeded")]
        DeviceMessageRateExceeded,
        
        [Description("Topics FcmMessage Rate Exceeded")]
        TopicsMessageRateExceeded,

        [Description("The sender account used to send a message couldn't be authenticated")]
        AuthenticationError,

        [Description("Invalid Json")]
        InvalidJson,
    }
}
