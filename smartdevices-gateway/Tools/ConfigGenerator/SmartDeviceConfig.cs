using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using SmartDevicesGateway.Common.Extensions;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Config.ValueSpecifications;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Tabs;
using SmartDevicesGateway.Model.Ui;

namespace ConfigGenerator
{
    public class SmartDeviceConfig
    {
        public IList<DeviceGroup> DeviceGroups { get; set; }
        public IList<User> Users { get; set; }
        public IList<TabConfig> TabConfig { get; set; }
        public IList<UiLayout> Uis { get; set; }
        public IList<UiComponent> Actions { get; set; }
        public IList<ConfigValueSpecification> ValueDefinitions { get; set; }
        public AppInfo AppInfo { get; set; }
    }

    public abstract class SmartDeviceConfigGenerator
    {
        public abstract SmartDeviceConfig GenerateConfig(SmartDeviceConfig conf);

        public SmartDeviceConfig GenerateConfig()
        {
            return GenerateConfig(new SmartDeviceConfig());
        }
    }

    public static class SmartDeviceConfigExtensions
    {
        public static SmartDeviceConfig MergeWith(this SmartDeviceConfig first, SmartDeviceConfig second)
        {
            return new SmartDeviceConfig
            {
                DeviceGroups     = MergeLists(first.DeviceGroups, second.DeviceGroups, x => x.GroupName),
                Users            = MergeLists(first.Users, second.Users, x => x.Username),
                TabConfig        = MergeLists(first.TabConfig, second.TabConfig, x => x.Key),
                Uis              = MergeLists(first.Uis, second.Uis, x => x.Id),
                Actions          = MergeLists(first.Actions, second.Actions, x => x.Id),
                ValueDefinitions = MergeLists(first.ValueDefinitions, second.ValueDefinitions, x => x.Name),
                AppInfo          = second.AppInfo ?? first.AppInfo,       
            };
        }

        private static IList<T> MergeLists<T, TE>(IEnumerable<T> first, IEnumerable<T> second, Func<T, TE> selection)
        {
            var comparer = EqualityComparerFactory<T>
                .CreateComparer(x => selection(x).GetHashCode(), (x, y) => x != null && x.Equals(y));

            var list = new List<T>();



            if (second != null && first != null)
            {
                list.AddRange(first.Where(x => !second.Contains(x, comparer)));
                list.AddRange(second);
            }
            else
            {
                if (first != null)
                {
                    list.AddRange(first);
                }
                if (second != null)
                {
                    list.AddRange(second);
                }
            }

            return list;
        }

        public static class EqualityComparerFactory<T>
        {
            private class MyComparer : IEqualityComparer<T>
            {
                private readonly Func<T, int> _getHashCodeFunc;
                private readonly Func<T, T, bool> _equalsFunc;

                public MyComparer(Func<T, int> getHashCodeFunc, Func<T, T, bool> equalsFunc)
                {
                    _getHashCodeFunc = getHashCodeFunc;
                    _equalsFunc = equalsFunc;
                }

                public bool Equals(T x, T y)
                {
                    return _equalsFunc(x, y);
                }

                public int GetHashCode(T obj)
                {
                    return _getHashCodeFunc(obj);
                }
            }

            public static IEqualityComparer<T> CreateComparer(Func<T, int> getHashCodeFunc, Func<T, T, bool> equalsFunc)
            {
                if (getHashCodeFunc == null)
                {
                    throw new ArgumentNullException("getHashCodeFunc");
                }

                if (equalsFunc == null)
                {
                    throw new ArgumentNullException("equalsFunc");
                }

                return new MyComparer(getHashCodeFunc, equalsFunc);
            }
        }
    }
}
