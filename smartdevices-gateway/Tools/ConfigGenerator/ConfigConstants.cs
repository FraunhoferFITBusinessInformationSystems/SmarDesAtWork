using System;
using System.Collections.Generic;
using System.Text;

namespace ConfigGenerator
{
    public static class ConfigConstants
    {
        public static class Tabs
        {
            public const string Actions = "actions";
            public const string Livedata = "livedata";
            public const string Dashboard = "dashboard";
        }

        public static class Components
        {
            public const string GenericAction = "GenericAction";
            public const string Spinner = "Spinner";
            public const string TextInput = "TextInput";
            public const string DoubleButton = "DoubleButton";
            public const string TextView = "TextView";
            public const string Button = "Button";
            public const string BarcodeInput = "BarcodeInput";
            public const string DateView = "DateView";
            public const string Switch = "Swtich";
            public const string NumberInput = "NumberInput";
            public const string Spacer = "Spacer";
            public const string ValueDisplayPlain = "ValueDisplayPlain";
            public const string GraphDisplay = "GraphDisplay";
            public const string ValueDisplayGauge = "ValueDisplayGauge";
            public const string ValueMonitor = "ValueMonitor";
        }

        public static class Views
        {
            public const string JobView = "JobView";
            public const string TodoList = "TodoList";
        }
    }
}
