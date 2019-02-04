using System;
using System.Collections.Generic;
using System.Text;
using Amqp;

namespace AmqpTestDriver
{
    public class WaveSimulator
    {
        private TimedClockSource Clock { get; set; }
        public readonly List<WaveProperties> WaveProps = new List<WaveProperties>();
        public readonly List<ValueConsumer> Consumers = new List<ValueConsumer>();

        public delegate void ValueConsumer(double x, double y, WaveProperties prop);

        public WaveSimulator(int interval)
        {
            Clock = new TimedClockSource()
            {
                Interval = interval,
                Callback = Callback
            };
        }

        private void Callback(double x)
        {
            foreach (var prop in WaveProps)
            {
                var waveX = x * prop.FMultiplier + prop.FOffset;
                var waveY = prop.Wave.F(waveX);

                foreach (var valueConsumer in Consumers)
                {
                    valueConsumer.Invoke(waveX, waveY, prop);
                }
            }
        }

        private void SendAmqpBodyMsg()
        {

        }

        public void StartSimulation()
        {
            Clock.Start();
        }

        public void StopSimulation()
        {
            Clock.Stop();
        }

        
    }

    public class WaveProperties
    {
        public string Key { get; set; }
        public string ValueName { get; set; }
        public string Unit { get; set; }
        public double SetPoint { get; set; }

        public double FMultiplier { get; set; } = 1;

        public bool DynamicSeverity { get; set; }
        public double SeverityWarning { get; set; }
        public double SeverityError { get; set; }

        public double FOffset { get; set; }
        
        public string PropName { get; set; }
        public string PropNamespace { get; set; }
        public string PropType { get; set; }

        public Func<WaveProperties, double, string> ValueFormatter { get; set; } = 
            (props, d) => string.Format("{0,7:0.00}", Math.Round(d, 2)).Replace(',', '.');

        public WaveFilter Wave { get; set; }
    }
}
