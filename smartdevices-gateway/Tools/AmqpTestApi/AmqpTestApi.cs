using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Amqp;
using Amqp.Framing;
using Microsoft.Extensions.Configuration;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace AmqpTestApi
{
    public class AmqpTestApi
    {
        public bool Running { get; set; }
        public IConfiguration Configuration { get; }
        public Connection Connection { get; set; }
        public Session ReceiverSession { get; set; }

        public string ClientName { get; set; }
        public string Queue { get; set; }
        public string ConnectionString { get; set; }

        public AmqpTestApi(IConfiguration configuration)
        {
            Configuration = configuration;
            ConnectionString = Configuration["ConnectionString"];
            ClientName = Configuration["ClientName"];
            Queue = Configuration["Queue"];
        }

        public async Task Start()
        {
            Running = true;
            Connection = new Connection(new Address(ConnectionString));
            ReceiverSession = new Session(Connection);
            var receiverLink = new ReceiverLink(ReceiverSession, $"{ClientName}-rc", Queue);

            while (Running)
            {
                var message = await receiverLink.ReceiveAsync(TimeSpan.FromMilliseconds(500));
                if (message == null)
                {
                    continue;
                }
                Console.WriteLine("Received Message");
                if (string.IsNullOrEmpty(message.Properties?.Subject) ||
                    string.IsNullOrEmpty(message.Properties.ReplyTo))
                {
                    receiverLink.Reject(message);
                    continue;
                }
                receiverLink.Accept(message);
                await ProcessMessage(message);
            }
        }

        private async Task ProcessMessage(Message message)
        {
            var apiMethods = new ApiMethods(message);
            Message response = null;

            

            switch (message.Properties.Subject)
            {
                case "ToDoFind":
                    response = apiMethods.ToDoFind();
                    break;
                default:
                    break;
            }

            if (response == null)
            {
                return;
            }

            if (response.Properties == null)
            {
                response.Properties = new Properties();
            }

            response.Properties.CorrelationId = message.Properties.CorrelationId;
            response.Properties.ReplyTo = null;
            response.Properties.Subject = message.Properties.Subject;

            var senderSession = new Session(Connection);
            var senderLink = new SenderLink(senderSession, $"{ClientName}-tx", message.Properties.ReplyTo);
            await senderLink.SendAsync(response);
            await senderLink.CloseAsync();
            await senderSession.CloseAsync();
            Console.WriteLine("Sendet Answer");
        }

        public void Stop()
        {
            Running = false;
            ReceiverSession?.Close();
            Connection?.Close();
        }
    }

    public class ApiMethods
    {
        public static readonly object TodoFindResponse = new
        {
            responseObject = new
            {
                headers = "test"
            }
        };

        public Message RequestMessage { get; }
        public JObject Body { get; }

        public ApiMethods(Message requestMessage)
        {
            RequestMessage = requestMessage;
            Body = DeserializeMessage(requestMessage.Body);
        }


        public Message ToDoFind()
        {
            var msg = new Message(SerializeMessage(TodoFindResponse));

            return msg;
        }

        private static byte[] SerializeMessage(object message)
        {
            var jsonText = JsonConvert.SerializeObject(message);
            var bytes = Encoding.UTF8.GetBytes(jsonText);

            return bytes;
        }

        private static JObject DeserializeMessage(object body)
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
            var response = JObject.Parse(jsonString);

            if (response == null)
            {
                throw new ApplicationException("Could not parse response Object");
            }

            return response;
        }
    }
}