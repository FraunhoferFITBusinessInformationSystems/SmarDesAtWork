//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Text;
using log4net;
using Newtonsoft.Json;
using Vogler.Amqp;

namespace SmartDevicesGateway.Api
{
    public static class CustomLoggerExtensions
    {
//
//
//        public static string PrintAmqpJsonMessage(this Message message)
//        {
//            var sb = new StringBuilder();
//            sb.Append("----------------------------------------------------------------------------\n");
//            sb.Append("Body........................: \n");
//            sb.Append(jsonText);
//            sb.Append("\n");
//            sb.Append("----------------------------------------------------------------------------\n");
//
//            return sb.ToString();
//        }

        public static void DebugLogAmqpMessage(this ILog logger, string message, Amqp.Message m,
            string queue = null)
        {
            try
            {
                var msg = AmqpUtils.DeserializeMessage(m.Body);
                var formattedJson = JsonConvert.SerializeObject(msg, DebugPrintSerializerSettings);
                var sb = new StringBuilder(message).Append("\n");
                sb.Append("----------------------------------------------------------------------------\n");
                if (queue != null)
                {
                    sb.Append(PadRight("Queue")).Append(queue).Append("\n");
                }

                if (m.Properties != null)
                {
                    if (m.Properties.ReplyTo != null)
                    {
                        sb.Append(PadRight("ReplyTo")).Append(m.Properties.ReplyTo).Append("\n");
                    }

                    if (m.Properties.Subject != null)
                    {
                        sb.Append(PadRight("Subject")).Append(m.Properties.Subject).Append("\n");
                    }

                    if (m.Properties.CorrelationId != null)
                    {
                        sb.Append(PadRight("CorrelationId")).Append(m.Properties.CorrelationId).Append("\n");
                    }

                    if (m.Properties.MessageId != null)
                    {
                        sb.Append(PadRight("Properties.MessageId")).Append(m.Properties.MessageId).Append("\n");
                    }
                }

                if (m.ApplicationProperties != null)
                {
                    foreach (var pair in m.ApplicationProperties.Map)
                    {
                        sb.Append(PadRight($"Application.{pair.Key}")).Append(pair.Value);
                    }
                }
                sb.Append(PadRight("Body")).Append(formattedJson).Append("\n");
                sb.Append("----------------------------------------------------------------------------\n");
                logger.Debug(sb.ToString());
            }
            catch (Exception e)
            {
                logger.Warn("Error in CusomLoggerExtensions while logging Amqp.Message", e);
            }
        }

        private static string PadRight(string input)
        {
            return input + ".............................: ".Substring(input.Length);
        }

        public static void DebugLogMessage(this ILog logger, string message, Model.Messages.Message m)
        {
            try
            {
                var jsonText = JsonConvert.SerializeObject(m, DebugPrintSerializerSettings);
                var sb = new StringBuilder(message).Append("\n");
                sb.Append("----------------------------------------------------------------------------\n");
                sb.Append(PadRight("Body")).Append(jsonText).Append("\n");
                sb.Append("----------------------------------------------------------------------------\n");
                logger.Debug(sb.ToString());
            }
            catch (Exception e)
            {
                logger.Warn("Error in CusomLoggerExtensions while logging Message", e);
            }
        }


        private static JsonSerializerSettings _debugPrintSerializerSettings = null;
        private static JsonSerializerSettings DebugPrintSerializerSettings => 
            _debugPrintSerializerSettings ?? 
            (_debugPrintSerializerSettings = new JsonSerializerSettings
        {
            Formatting = Formatting.Indented
        });
    }
}
