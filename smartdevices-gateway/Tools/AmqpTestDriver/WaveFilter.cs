using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace AmqpTestDriver
{
    public interface IWaveFunc
    {
        double F(double x);
    }

    public class WaveFilter : IWaveFunc
    {
        public double LastF { get; private set; } = double.NaN;
        public WaveFilter Input { get; set; }

        public WaveFilter(WaveFilter input)
        {
            Input = input;
        }

        public virtual double F(double x)
        {
            var d = Input.F(x);
            LastF = d;
            return d;
        }
    }

    public class RandomNoiseFilter : WaveFilter
    {
        public double Amplitude { get; set; }
        private readonly Random _random = new Random((int)DateTimeOffset.Now.Ticks);

        public RandomNoiseFilter(WaveFilter input, double amplitude) : base(input)
        {
            Amplitude = amplitude;
        }

        public override double F(double x)
        {
            var f = base.F(x);
            var r = (_random.NextDouble() * Amplitude * 2) - Amplitude;
            return f + r;
        }
    }

    public class AbsoluteFuncFilter : WaveFilter
    {
        public AbsoluteFuncFilter(WaveFilter input) : base(input)
        {
        }

        public override double F(double x)
        {
            return Math.Abs(base.F(x));
        }
    }

    public class InvertFuncFilter : WaveFilter
    {
        public InvertFuncFilter(WaveFilter input) : base(input)
        {
        }

        public override double F(double x)
        {
            return base.F(x) * -1;
        }
    }

    public class AmplificationFilter : WaveFilter
    {
        public double Amplification { get; set; } = 1;

        public AmplificationFilter(WaveFilter input) : base(input)
        {
        }

        public AmplificationFilter(WaveFilter input, double amplification) : base(input)
        {
            Amplification = amplification;
        }

        public override double F(double x)
        {
            return base.F(x) * Amplification;
        }
    }

    public class StepIncrementFilter : WaveFilter
    {
        public double StepSize { get; set; } = 1;

        public StepIncrementFilter(WaveFilter input) : base(input)
        {
        }

        public StepIncrementFilter(WaveFilter input, double stepSize) : base(input)
        {
            StepSize = stepSize;
        }

        public override double F(double x)
        {
            var a = base.F(x) / StepSize;
            return Math.Floor(a) * StepSize;
        }
    }

    public class ConstantOffsetFilter : WaveFilter
    {
        public double Offset { get; set; }

        public ConstantOffsetFilter(WaveFilter input) : base(input)
        {
        }

        public ConstantOffsetFilter(WaveFilter input, double offset) : base(input)
        {
            Offset = offset;
        }

        public override double F(double x)
        {
            return base.F(x) + Offset;
        }
    }

    public class CombinationFilter : WaveFilter
    {
        public WaveFilter Input2 { get; set; }
        public CombinationType Type { get; set; }

        public CombinationFilter(WaveFilter input1, WaveFilter input2, CombinationType type) : base(input1)
        {
            Input2 = input2;
            Type = type;
        }

        public CombinationFilter(WaveFilter input1, WaveFilter input2) : base(input1)
        {
            Input2 = input2;
            Type = CombinationType.Add;
        }

        public override double F(double x)
        {
            var a = base.F(x);
            var b = Input2.F(x);

            switch (Type)
            {
                case CombinationType.Add:
                    return a + b;
                case CombinationType.Substract:
                    return a - b;
                case CombinationType.Multiply:
                    return a * b;
                case CombinationType.Divide:
                    // ReSharper disable once CompareOfFloatsByEqualityOperator
                    if (b == 0.0)
                    {
                        return double.NaN;
                    }
                    return a / b;
                default:
                    return double.NaN;
            }
        }
    }

    public class WaveGenerator : WaveFilter
    {
        private readonly Random _random = new Random((int)DateTimeOffset.Now.Ticks);

        public double Phase { get; set; }
        public double Frequency { get; set; } = 1.0f;
        public GeneratorSignalType SignalType { get; set; } = GeneratorSignalType.Sine;

        public WaveGenerator() : base(null)
        {
        }

        public WaveGenerator(GeneratorSignalType signalType) : base(null)
        {
            SignalType = signalType;
        }

        public WaveGenerator(GeneratorSignalType signalType, double phase, double frequency) : base(null)
        {
            SignalType = signalType;
            Phase = phase;
            Frequency = frequency;
        }
        
        public override double F(double x)
        {
            double value;
            var t = x * Frequency + Phase;
            switch (SignalType)
            {
                case GeneratorSignalType.Sine:
                    value = Math.Sin(2f * Math.PI * t);
                    break;
                case GeneratorSignalType.Square:
                    value = Math.Sign(Math.Sin(2f * Math.PI * t));
                    break;
                case GeneratorSignalType.Triangle:
                    value = 1f - 4f * Math.Abs(Math.Round(t - 0.25f) - (t - 0.25f));
                    break;
                case GeneratorSignalType.Sawtooth:
                    value = 2f * (t - Math.Floor(t + 0.5f));
                    break;
                case GeneratorSignalType.Random:
                    value = _random.NextDouble();
                    break;
                default:
                    throw new ArgumentOutOfRangeException();
            }
            return value;
        }
    }

    [JsonConverter(typeof(StringEnumConverter))]
    public enum CombinationType
    {
        Add,
        Substract,
        Multiply,
        Divide
    }

    [JsonConverter(typeof(StringEnumConverter))]
    public enum GeneratorSignalType
    {
        Sine,
        Square,
        Triangle,
        Sawtooth,
        Random
    }
}
