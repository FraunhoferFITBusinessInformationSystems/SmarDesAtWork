//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using SmartDevicesGateway.Services.FcmService.Requests;

namespace SmartDevicesGateway.Services.FcmService
{
    public class FcmMessageBuilder
    {
        private readonly List<string> _deviceTokens = new List<string>();
        private FcmMessage _msg = new FcmMessage();

        public FcmMessageBuilder AddReceiver(string token)
        {
            _deviceTokens.Add(token);
            return this;
        }

        public FcmMessageBuilder AddReceiver(params string[] tokens)
        {
            foreach (var token in tokens)
            {
                AddReceiver(token);
            }
            return this;
        }

        public FcmMessageBuilder SetNotification(FcmNotification notification)
        {
            _msg.FcmNotification = notification;
            return this;
        }

        public FcmMessageBuilder SetData(object obj)
        {
            _msg.Data = obj;
            return this;
        }

        public FcmMessageBuilder SetDebug(bool debug = true)
        {
            _msg.DryRun = debug;
            return this;
        }

        public FcmMessageBuilder SetTtl(int ttl)
        {
            _msg.TimeToLive = ttl;
            return this;
        }

        public FcmMessageBuilder SetPriority(FcmMessagePriority priority)
        {
            _msg.Priority = priority;
            return this;
        }

        public FcmMessageBuilder SetCollapseKey(string collapseKey)
        {
            _msg.CollapseKey = collapseKey;
            return this;
        }

        public FcmMessageBuilder SetRestrictedPackageName(string restrictedPackageName)
        {
            _msg.RestrictedPackageName = restrictedPackageName;
            return this;
        }

        /// <summary>
        /// Builds the <see cref="FcmMessage" /> and returns it.
        /// </summary>
        /// <returns>A sendable <see cref="FcmMessage" />-object.</returns>
        /// <exception cref="System.Exception">Thrown when the message would not be valid.</exception>
        public FcmMessage Build()
        {
            _msg.RegistrationIds = new List<string>();
            foreach (var token in _deviceTokens)
            {
                _msg.RegistrationIds.Add(token);
            }

            //Error: 400 Bad Request when registration_ids is empty!
            //Send Multiple Notifications when RegistrationIds.Count()>=1000
            var count = _msg.RegistrationIds.Count;
            if (count == 0 || count >= 1000)
            {
                throw new FcmMessageBuilderException($"FcmMessage creation failed. Invalid token count: {count}");
            }

            var msg = _msg;
            _msg = null;

            return msg;
        }

        public FcmMessageBuilder Reset()
        {
            _msg = new FcmMessage();
            _deviceTokens.Clear();
            return this;
        }
    }

    public class FcmMessageBuilderException : Exception
    {
        public FcmMessageBuilderException(string message) : base(message)
        {
        }

        public FcmMessageBuilderException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
