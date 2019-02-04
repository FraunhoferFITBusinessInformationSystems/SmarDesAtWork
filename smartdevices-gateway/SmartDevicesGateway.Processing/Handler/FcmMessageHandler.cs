//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using log4net;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Notification;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Services.ConfigService;
using SmartDevicesGateway.Services.FcmService;
using SmartDevicesGateway.Services.FcmService.Requests;
using SmartDevicesGateway.Services.FcmService.Responses;
using static SmartDevicesGateway.Model.Notification.FcmMessageBody;

namespace SmartDevicesGateway.Processing.Handler
{
    public class FcmMessageHandler
    {
        private static readonly ILog Logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        private readonly IPersistenceProvider _persistenceProvider;
        private readonly IConfigService _configService;
        private readonly IFcmService _fcmService;

        public FcmMessageHandler(IPersistenceProvider persistenceProvider, IConfigService configService, IFcmService fcmService)
        {
            _persistenceProvider = persistenceProvider;
            _configService = configService;
            _fcmService = fcmService;
        }

        public bool SendGetDataWithNotification(params DeviceId[] deviceIds)
        {
            return SendFcmMessage(new FcmMessageBody { Action = Actions.GetData, Notification = true}, deviceIds);
        }

        public bool SendGetConfig(params DeviceId[] deviceIds)
        {
            return SendFcmMessage(new FcmMessageBody { Action = Actions.GetConfig }, deviceIds);
        }

        public bool SendGetDataWithoutNotification(params DeviceId[] deviceIds)
        {
            return SendFcmMessage(new FcmMessageBody { Action = Actions.GetData }, deviceIds);
        }

        public bool SendGetAllWithoutNotification(params DeviceId[] deviceIds)
        {
            return SendFcmMessage(new FcmMessageBody{ Action = Actions.GetAll }, deviceIds);
        }

        public bool SendGetDataWithNotification(int pattern, params DeviceId[] deviceIds)
        {
            return SendFcmMessage(new FcmMessageBody {Action = Actions.GetData, Notification = true, Pattern = pattern}, deviceIds);
        }

        public bool SendFcmMessage(object data, params DeviceId[] deviceIds)
        {
            var tokens = new LinkedList<string>();

            foreach (var deviceId in deviceIds)
            {
                if (_persistenceProvider.TryGetDeviceInfos(deviceId, out var deviceInfo))
                {
                    tokens.AddLast(deviceInfo.FcmToken);
                }
                else
                {
                    Logger.Info($"DeviceToken for Id: {deviceId?.FullId} not found!");
                }
            }

            var tokenArr = tokens.Where(x => x != null).ToArray();
            if (tokens.Count > 0)
            {
                try
                {
                    var msg = new FcmMessageBuilder()
                        .AddReceiver(tokenArr)
                        .SetDebug(false)
                        .SetPriority(FcmMessagePriority.High)
                        .SetData(data)
                        .Build();

                    var response = _fcmService.Send(msg);

                    if (response.Error == ResponseError.NoError)
                    {
                        Logger.Debug(
                            $"FCM Response: [Success={response.ResponseMessage?.Success}, Failure={response.ResponseMessage?.Failure}]");

                        return true;
                    }

                    Logger.Error($"FCM Response Error - Code: {response.ErrorCode}, \"{response.ErrorMessage}\"");
                    return false;
                }
                catch (FcmMessageBuilderException e)
                {
                    Logger.Error("FCM Builder encountered an Error:", e);
                    return false;
                }
            }
            Logger.Debug("FcmNotification: Found no devices for notification");
            return true;
        }
    }
}

