//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Vogler.Amqp
{
    public class AmqpUtils
    {
        public static byte[] SerializeMessage(object message)
        {
            var jsonText = JsonConvert.SerializeObject(message);
            var bytes = Encoding.UTF8.GetBytes(jsonText);
            return bytes;
        }

        public static JObject DeserializeMessage(object body)
        {
            if (body == null)
            {
                return null;
            }

            if (!(body is byte[] bytes))
            {
                return null;
            }

            var jsonString = Encoding.UTF8.GetString(bytes);
            if (string.IsNullOrEmpty(jsonString))
            {
                return null;
            }

            try
            {
                return JObject.Parse(jsonString);
            }
            catch (JsonReaderException)
            {
                return null;
            }
        }
    }
}
