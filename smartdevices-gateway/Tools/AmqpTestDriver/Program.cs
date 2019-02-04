using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Reflection;
using System.Text;
using Amqp;
using Amqp.Framing;
using Microsoft.Extensions.Configuration;

namespace AmqpTestDriver
{
    class Program
    {
        private static readonly List<KeyValuePair<string, string>> DefaultConfig =
            new List<KeyValuePair<string, string>>
            {
                new KeyValuePair<string, string>("ConnectionString", "amqp://artemis:artemis@localhost:5672"),
                new KeyValuePair<string, string>("ClientName", "AmqpTestDriver"),
                new KeyValuePair<string, string>("PLCDataQueue", "PLCData"),
                new KeyValuePair<string, string>("SDGWQueue", "sdgw"),
                new KeyValuePair<string, string>("RuleEngineQueue", "RuleEngine")
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

            //            OldSimulation.ValueSimulator(args);
            //            OldSimulation.WaveGeneratorTest(args);
            WaveGenerator(configuration);
        }

        static void WaveGenerator(IConfiguration config)
        {
            var connectionString = config["ConnectionString"];
            var clientName = config["ClientName"];
            var plcDataQueueName = config["PLCDataQueue"];
            var sdgwQueueName = config["SDGWQueue"];

            var connection = new Connection(new Address(connectionString));
            var session = new Session(connection);
            PlcDataLink = new SenderLink(session, $"{clientName}-plc", plcDataQueueName);
            MessageLink = new SenderLink(session, $"{clientName}-sdgw", sdgwQueueName);

            var sim = new WaveSimulator(500);
            sim.Consumers.Add(DebugPrint);
            sim.Consumers.Add(SendAmqpMessages);

            sim.WaveProps.Add(new WaveProperties
                {
                    ValueName = "m00001744:Massedruck",
                    Key = "Massedruck",
                    Unit = "Bar",
                    SetPoint = 12.24,
                   
                    PropName = "Pressure",
                    PropNamespace = "Test",
                    PropType = "ValueData",

                    FMultiplier = 0.4,
                    FOffset = 0,

                    DynamicSeverity = true,
                    SeverityWarning = 1.3,
                    SeverityError = 1.8,

                Wave = new RandomNoiseFilter(new ConstantOffsetFilter(new AmplificationFilter(new WaveGenerator(GeneratorSignalType.Sawtooth, 0, 0.01), 2.0), 11.8), 0.01)
                });
            sim.WaveProps.Add(new WaveProperties
            {
                ValueName = "m00001744:Massetemperatur",
                Key = "Temperatur",
                //Unit = "\u00B0C",
                Unit = "C",
                SetPoint = 161.2,

                PropName = "Temperature",
                PropNamespace = "Test",
                PropType = "ValueData",

                FMultiplier = 0.35,
                FOffset = 0,

                DynamicSeverity = true,
                SeverityWarning = 1.3,
                SeverityError = 1.8,

                Wave = new RandomNoiseFilter(new ConstantOffsetFilter(new AmplificationFilter(new WaveGenerator(GeneratorSignalType.Sine, 0, 0.01), 2.0), 161), 0.01)
            });
            sim.WaveProps.Add(new WaveProperties
            {
                ValueName = "m00001744:Drehzahl",
                Key = "Drehzahl",
                //Unit = "\u00B0C",
                Unit = "RPM",
                SetPoint = 5020,

                PropName = "Drehzahl",
                PropNamespace = "Test",
                PropType = "ValueData",

                FMultiplier = 0.02,
                FOffset = 0,

                DynamicSeverity = true,
                SeverityWarning = 6.5,
                SeverityError = 7.5,

                Wave = new RandomNoiseFilter(new ConstantOffsetFilter(new AmplificationFilter(new WaveGenerator(GeneratorSignalType.Sine, 0, 0.01), 8.0), 5020), 0.01)
            });
            sim.WaveProps.Add(new WaveProperties
            {
                ValueName = "m00001744:Aussendurchmesser",
                Key = "Aussendurchmesser",
                //Unit = "\u00B0C",
                Unit = "mm",
                SetPoint = 12.5,

                PropName = "Drehzahl",
                PropNamespace = "Test",
                PropType = "ValueData",

                FMultiplier = 0.5,
                FOffset = 0,

                DynamicSeverity = true,
                SeverityWarning = 0.2,
                SeverityError = 0.3,

                Wave = new RandomNoiseFilter(new ConstantOffsetFilter(new AmplificationFilter(new WaveGenerator(GeneratorSignalType.Sine, 0, 0.01), 0.3), 12.4), 0.005)
            });

            sim.StartSimulation();

            var running = true;
            do
            {
                var consoleKeyInfo = Console.ReadKey();
                if (consoleKeyInfo.Key == ConsoleKey.C && (consoleKeyInfo.Modifiers & ConsoleModifiers.Control) != 0)
                {
                    running = false;
                }
            } while (running);
            sim.StopSimulation();

            PlcDataLink.Close();
            MessageLink.Close();
            session.Close();
            connection.Close();
        }

        public static SenderLink PlcDataLink { get; set; }
        public static SenderLink MessageLink { get; set; }

        private static void DebugPrint(double x, double y, WaveProperties prop)
        {
            Console.WriteLine(string.Format("Sending: ({0,7:F3} | {1,7:F3} ) as {2}", x, y, prop.ValueName));
        }

        private static void SendAmqpMessages(double x, double y, WaveProperties prop)
        {
            var value = prop.ValueFormatter(prop, y);
            var setPoint = prop.ValueFormatter(prop, prop.SetPoint);

            Func<double, string> formatter = (d) => string.Format("{0:0.000000}", Math.Round(d, 6)).Replace(',', '.');

            var sev = "Normal";
            double h = 0, hh = 0, l = 0, ll = 0;

            if (prop.DynamicSeverity)
            {
                var diff = Math.Abs(prop.SetPoint - y);
                if (diff >= prop.SeverityError)
                {
                    sev = "Error";
                }
                else if (diff >= prop.SeverityWarning)
                {
                    sev = "Warning";
                }

                hh = prop.SetPoint + prop.SeverityError;
                h = prop.SetPoint + prop.SeverityWarning;
                l = prop.SetPoint - prop.SeverityWarning;
                ll = prop.SetPoint - prop.SeverityError;
            }

            //Body Message
            string body = $@"{{
    ""Id"": ""{Guid.NewGuid()}"",
    ""Key"" : ""{prop.ValueName}"",
    ""Type"": ""ValueMessage"",
    ""Source"": ""ValueUpdate"",
    ""ValueName"": ""{prop.ValueName}"",
    ""Name"": ""{prop.Key}"",
    ""Value"": ""{value.Trim()}"",
    ""HH"": {formatter(hh)},
	""H"":  {formatter(h)},
	""L"":  {formatter(l)},
	""LL"": {formatter(ll)},
    ""Unit"": ""{prop.Unit}"",
    ""SetPoint"": ""{setPoint}"",
    ""Severity"": ""{sev}""}}";

            var message1 = new Message
            {
                BodySection = new Data
                {
                    Binary = Encoding.UTF8.GetBytes(body)
                }
            };
            MessageLink.Send(message1);

            //PlcDataMessage
            var message2 = new Message
            {
                Properties = new Amqp.Framing.Properties
                {
                    CorrelationId = Guid.NewGuid().ToString(),
                },
                ApplicationProperties = new ApplicationProperties
                {
                    Map =
                    {
                        {"Type", prop.PropType},
                        {"Name", prop.PropName},
                        {"Value", value},
                        {"Namespace", prop.PropNamespace},
                        {"Timestamp", DateTime.Now.ToString(CultureInfo.InvariantCulture)},
                    }
                }
            };
            PlcDataLink.Send(message2);
        }
    }
}