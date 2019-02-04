using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Linq;

namespace AmqpTestDriver
{
    public class WaveBuilder
    {
        private WaveFilter _filter;

        public WaveBuilder()
        {

        }

        public void Parse(JObject jObject)
        {

        }

        public void AddWaveFilter(WaveFilterConfig config)
        {

        }

        public WaveFilter Build()
        {
            return _filter;
        }
    }

    [JsonConverter(typeof(StringEnumConverter))]
    public enum WaveFilterTypes
    {
        RandomNoise,
        Absolute,
        Invert,
        Amplification,
        ConstantOffset,
        Combination,
        Generator
    }

    public class WaveFilterConfig
    {
        public WaveFilterTypes Type { get; set; }
        public double Amplitude { get; set; }
        public WaveFilterConfig Source { get; set; }

        [JsonExtensionData]
        public Dictionary<string, JObject> AdditionalProperties { get; set; } = new Dictionary<string, JObject>();
    }
}
