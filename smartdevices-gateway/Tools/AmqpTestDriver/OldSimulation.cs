using System;
using System.Collections.Generic;
using System.Globalization;
using System.Reactive.Linq;
using System.Text;
using System.Threading;
using Amqp;
using Amqp.Framing;
using Mono.Options;

namespace AmqpTestDriver
{
    public class OldSimulation
    {
        public static void WaveGeneratorTest(string[] args)
        {
            var wf = new WaveFactory();
            wf.StartWaveGeneration();

            var running = true;

            do
            {
                var consoleKeyInfo = Console.ReadKey();

                if (consoleKeyInfo.Key == ConsoleKey.C && (consoleKeyInfo.Modifiers & ConsoleModifiers.Control) != 0)
                {
                    running = false;
                }
            } while (running);

            wf.StopWaveGenerator();
        }

        public static void ValueSimulator(string[] args)
        {
            string amqpConnection = "amqp://user:password@localhost:5672";
            string parameterName = null;
            string parameterValue = null;
            string sineWave = "Temperature";
            string queueName = "sdgw";

            var p = new OptionSet
            {
                {"amqpConnection=", "", v => amqpConnection = v},
                {"parameterName=", "", v => parameterName = v},
                {"parameterValue=", "", v => parameterValue = v},
                {"sineWave=", "", v => sineWave = v},
                {"queueName=", "", v => queueName = v}
            };

            var sender = Connect(amqpConnection, queueName);

            while (true)
            {
                //                var body = $@"{{
                //                            ""Id"": ""{Guid.NewGuid()}"",
                //                            ""Key"" : ""Massedruck"",
                //                            ""Type"": ""ValueMessage"",
                //                            ""Source"": ""ValueUpdate"",
                //                            ""ValueName"": ""m00001744:Massedruck"",
                //                            ""Value"": {new Random().NextDouble().ToString("F3", new CultureInfo("en-US"))},
                //                            ""Unit"": ""Pa"",
                //                            ""SetPoint"": {new Random().NextDouble().ToString("F3", new CultureInfo("en-US"))},
                //                            ""DynamicSeverity"": ""Normal""}}";
                for (var i = 0; i < 3; i++)
                {
                    var message1 = new Message
                    {
                        BodySection = new Data
                        {
                            Binary = Encoding.UTF8.GetBytes(getMessage(i))
                        }
                    };

                    sender.Send(message1);
                }

                Thread.Sleep(500);
            }
        }

        private static readonly string[] _keys = new[] {"Massedruck", "Massetemperatur", "Drehzahl"};

        private static readonly string[] _valueNames = new[]
            {"m00001744:Massedruck", "m00001744:Massetemperatur", "m00001744:Drehzahl"};

        private static readonly string[] _units = new[] {"Bar", @"°C", "RPM"};

        private static readonly Func<string>[] _values = new[]
        {
            (Func<string>) (() => (new Random().NextDouble() * 400 + 800).ToString("F2", new CultureInfo("en-US"))),
            (Func<string>) (() => (new Random().NextDouble() * 120 + 200).ToString("F2", new CultureInfo("en-US"))),
            (Func<string>) (() => (new Random().NextDouble() * 20 + 120).ToString("F3", new CultureInfo("en-US")))
        };

        private static readonly Func<string>[] _setPoints = new[]
        {
            (Func<string>) (() => (new Random().NextDouble() * 400 + 800).ToString("F2", new CultureInfo("en-US"))),
            (Func<string>) (() => (new Random().NextDouble() * 120 + 200).ToString("F2", new CultureInfo("en-US"))),
            (Func<string>) (() => (new Random().NextDouble() * 20 + 120).ToString("F3", new CultureInfo("en-US")))
        };

        private static string getMessage(int idx)
        {
            return $@"{{
    ""Id"": ""{Guid.NewGuid()}"",
    ""Key"" : ""{_keys[idx]}"",
    ""Type"": ""ValueMessage"",
    ""Source"": ""ValueUpdate"",
    ""ValueName"": ""{_valueNames[idx]}"",
    ""Value"": {_values[0].Invoke()},
    ""Unit"": ""{_units[idx]}"",
    ""SetPoint"": {_setPoints[idx].Invoke()},
    ""DynamicSeverity"": ""Normal""}}";
        }

        private static void SineWave(SenderLink sender, string parameterName)
        {
            var period = 1000 / 0.5; //0.5 Hz
            var cycles = 4; //cycles to display
            var quantization = 1000; //cycles to display            
            var amplitude = 10; //signal peak        
            var offset = 40;

            var range = quantization * cycles; //full range 

            //Sine wave generator for n cycles
            //makes tuple of (t, sin(t))
            var source = Observable.Interval(TimeSpan.FromMilliseconds(period / range))
                .Select(s => s % (range + 1))
                .Select(s => Tuple.Create(s,
                    amplitude * Math.Sin((double) s / ((double) range / (double) cycles) * 2 * Math.PI) + offset));

            using (
                source
                    .Sample(TimeSpan.FromMilliseconds(250))
                    .Subscribe(y =>
                    {
                        var message1 = new Message
                        {
                            ApplicationProperties = new ApplicationProperties()
                        };

                        message1.ApplicationProperties.Map.Add("SensorId", "s1");
                        message1.ApplicationProperties.Map.Add("ParameterName", parameterName);
                        message1.ApplicationProperties.Map.Add("ParameterValue", Math.Round(y.Item2, 2));

                        sender.Send(message1);
                    }))
            {
                Console.WriteLine("Press any key to unsubscribe");
                Console.ReadKey();
            }
        }

        private static SenderLink Connect(string amqpConnection, string queueName)
        {
            //var address = "amqp://vogler:23cAsAYd@54.93.111.16:5672";

            var connection = new Connection(new Address(amqpConnection));
            var session = new Session(connection);

            return new SenderLink(session, "test-sender", queueName);
        }

        public static void IntervalSimple()
        {
            var observable = Observable.Interval(TimeSpan.FromMilliseconds(50));

            // Sample the sequence every second
            using (observable.Sample(TimeSpan.FromSeconds(1)).Timestamp().Subscribe(
                x => Console.WriteLine("{0}: {1}", x.Value, x.Timestamp)))
            {
                Console.WriteLine("Press any key to unsubscribe");
                Console.ReadKey();
            }

            Console.WriteLine("Press any key to exit");
            Console.ReadKey();
        }
    }
}
