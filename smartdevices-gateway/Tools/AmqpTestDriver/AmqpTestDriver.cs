using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using Amqp;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace AmqpTestDriver
{
//    public class AmqpTestDriver
//    {
//        private AmqpTestDriverConfig _settings;
//        private readonly Dictionary<string, Wave> _waves = new Dictionary<string, Wave>();
//        private readonly Dictionary<string, SenderLink> _links = new Dictionary<string, SenderLink>();
//        private Connection _connection;
//        private Session _session;
//
//        public void Init()
//        {
//            Console.WriteLine("Reading configuration...");
//            _settings = JsonConvert.DeserializeObject<AmqpTestDriverConfig>(File.ReadAllText(@"DriverConfig.json"));
//            Console.WriteLine("Connecting to Amqp-Server...");
//            ConnectToAmqp(_settings.ConnectionString);
//        }
//
//        private void ConnectToAmqp(string amqpConnection)
//        {
//            _connection = new Connection(new Address(amqpConnection));
//            _session = new Session(_connection);
//        }
//
//        private SenderLink GetSenderLink(string queueName)
//        {
//            if (_links.ContainsKey(queueName) && _links[queueName] != null && !_links[queueName].IsClosed)
//            {
//                return _links[queueName];
//            }
//            _links[queueName] = new SenderLink(_session, _settings.ClientName, queueName);
//            return _links[queueName];
//        }
//
//        public void Start()
//        {
//            foreach (var generator in _settings.Generators)
//            {
//                var wave = new Wave
//                {
//                    SenderLink = GetSenderLink(generator.QueueName),
//                    ClockSource = new TimedClockSource()
//                };
//                _waves[generator.Namespace + ":" + generator.Name] = wave;
//
//                wave.BuildFunc(generator.ValueType);
//
//                wave.ClockSource.Start();
//            }
//        }
//    }

//    public class Wave
//    {
//        public SenderLink SenderLink { get; set; }
//        public ClockSource ClockSource { get; set; }
//        public WaveGenerator WaveGenerator { get; set; }
//        public IWaveFunc WaveFunc { get; set; }
//
//        public void BuildFunc(string generatorValueType)
//        {
//            throw new NotImplementedException();
//        }
//    }

    public class AmqpTestDriverConfig
    {
        public string ConnectionString { get; set; }
        public string ClientName { get; set; }

        public ICollection<ValueGeneratorConfig> Generators { get; set; }
    }

//    [JsonConverter(typeof(StringEnumConverter))]
//    public enum GeneratorSignalType
//    {
//        Sine,
//        Square,
//        Triangle,
//        Sawtooth,
//        Random
//    }
//
//    [JsonConverter(typeof(StringEnumConverter))]
//    public enum GeneratorFunctions
//    {
//        Add,
//        Substract,
//        Multiply,
//        Randomize,
//        Absolute,
//        Invert
//    }

    public class ValueGeneratorConfig
    {
        public string QueueName { get; set; }
        public string Namespace { get; set; }
        public string Name { get; set; }
        public string ValueType { get; set; }
    }
}
