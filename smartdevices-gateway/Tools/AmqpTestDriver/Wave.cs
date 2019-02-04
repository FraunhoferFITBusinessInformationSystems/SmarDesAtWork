using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;
using System.Threading;

namespace AmqpTestDriver
{

    public class WaveFactory
    {
        public double Amplitude { get; set; } = 90;

        public static WaveFilter SampleWave()
        {
            var freq = 0.01;

            var filter1 = new AmplificationFilter(new WaveGenerator(GeneratorSignalType.Sine, 0, freq), 0.4);
            var filter2 = new AmplificationFilter(new WaveGenerator(GeneratorSignalType.Sine, 90, freq*3.5), 0.2);
            var filter3 = new AmplificationFilter(new WaveGenerator(GeneratorSignalType.Sine, 180, freq*1.2), 0.3);
            var filter4 = new CombinationFilter(filter1, filter2, CombinationType.Add);
            var filter = new CombinationFilter(filter3, filter4, CombinationType.Substract);
            var noised = new RandomNoiseFilter(filter, 0.02);

            return noised;
        }
 
        public WaveFilter Wave { get; set; }
        public TimedClockSource Clk { get; set; }

        public WaveFactory()
        {
        }

        public void StartWaveGeneration()
        {
            Wave = new AmplificationFilter(SampleWave(), Amplitude);
            Clk = new TimedClockSource
            {
                Callback = x =>
                {
                    var y = Wave.F(x);
                    WriteValue(x, y);
                },
                Interval = 500
            };

            Clk.Start();
        }

        public void StopWaveGenerator()
        {
            Clk.Stop();
        }

        public void WriteValue(double x, double y)
        {
            const int charWidth = 180;

            var sb = new StringBuilder();

            var xstr = string.Format("{0,7:F3}", x);
            var ystr = string.Format("{0,7:F3}", y);

            sb.Append('(');
            sb.Append(xstr).Append('|');
            sb.Append(ystr).Append(") ");

            var startLen = sb.Length + 1;

            sb.Append('|');
            for (var i = 0; i < charWidth; i++)
            {
                sb.Append(' ');
                if (i == charWidth / 2)
                {
                    sb.Append('.');
                }
            }
            sb.Append('|');

            var mid = (int)charWidth / 2;
            var yy = ((y * Amplitude) / mid);
            if (yy > Amplitude)
            {
                yy = Amplitude;
            }
            if (yy < -Amplitude)
            {
                yy = -Amplitude;
            }

            var idx = (int) (yy + 0.49) + startLen + mid;


            sb[idx] = '*';
            sb.Append('\n');

            System.Console.Write(sb);
        }
    }

    public class TimedClockSource
    {
        private Timer _timer;
        private long _startTime;
        private readonly float _ticksPerSecond = Stopwatch.Frequency;

        public TickCallback Callback { get; set; }

        public delegate void TickCallback(double x);

        public int Interval { get; set; } = 100;

        private void Timer_Tick(object state)
        {
            var x = (double)((Stopwatch.GetTimestamp() - _startTime)
                            / _ticksPerSecond);
            Callback?.Invoke(x);
        }

        public void Start()
        {
            if (_timer != null)
            {
                throw new InvalidOperationException();
            }
            _startTime = Stopwatch.GetTimestamp();
            _timer = new Timer(Timer_Tick, null, 0, Interval);
        }

        public void Stop()
        {
            if (_timer == null)
            {
                throw new InvalidOperationException();
            }
            _timer.Dispose();
            _timer = null;
        }

        public void Reset()
        {
            _startTime = Stopwatch.GetTimestamp();
            if (_timer == null)
            {
                Start();
            }
            else
            {
                _timer.Change(0, Interval);
            }
        }
    }
}
