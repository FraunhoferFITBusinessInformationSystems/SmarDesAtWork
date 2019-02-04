using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Text;
using System.Threading;
using Microsoft.Extensions.Configuration;

namespace AmqpTestApi
{
    public class Program
    {
        private static readonly List<KeyValuePair<string, string>> DefaultConfig =
            new List<KeyValuePair<string, string>>
            {
                new KeyValuePair<string, string>("ConnectionString", "amqp://user:password@localhost:5672"),
                new KeyValuePair<string, string>("ClientName", "AmqpTestDriver"),
                new KeyValuePair<string, string>("Queue", "ToDo2")
            };

        static void Main(string[] args)
        {
            var pathToContentRoot = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);

            var builder = new ConfigurationBuilder()
                .SetBasePath(pathToContentRoot)
                .AddInMemoryCollection(DefaultConfig)
                .AddEnvironmentVariables()
                .AddJsonFile("ApplicationSettings.json", optional: true, reloadOnChange: true)
                .AddCommandLine(args);

            var configuration = builder.Build();

            var api = new AmqpTestApi(configuration);
            var thread = new Thread(async () => await api.Start());
            thread.Start();

            do
            {
                var consoleKeyInfo = Console.ReadKey();
                if (consoleKeyInfo.Key == ConsoleKey.C && (consoleKeyInfo.Modifiers & ConsoleModifiers.Control) != 0)
                {
                    api.Running = false;
                }
            } while (api.Running);

            api.Stop();
        }
    }
}
