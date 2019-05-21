using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using ConfigGenerator.Configs.General;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Config.SDConfig.Tabs;
using SmartDevicesGateway.Model.Config.ValueSpecifications;
using SmartDevicesGateway.Model.Enums;
using SmartDevicesGateway.Model.Ui;
using SmartDevicesGateway.Model.Values;

namespace ConfigGenerator.Configs.UseCases
{
    public class Uc5LiveData : SmartDeviceConfigGenerator
    {
        public const string GroupLiveData = "LiveData";
        public override SmartDeviceConfig GenerateConfig(SmartDeviceConfig conf)
        {
            #region ValueDefinitions

            const string queue = "sdgw";
            var valueDefs = new List<ConfigValueSpecification>();
            var valueActions = new List<UiComponent>();
            string[] m = { "mEQ1", "mEQ2" };
            for (var i = 0; i < m.Length; i++)
            {
                valueDefs.Add(new ConfigValueSpecification
                {
                    Name = $"{m[i]}:NumRevolutions",
                    DataSource = new ValueDataSource
                    {
                        QueueName = queue,
                        Namespace = m[i],
                        Name = "NumRevolutions"
                    }
                });
                valueDefs.Add(new ConfigValueSpecification
                {
                    Name = $"{m[i]}:Pressure",
                    DataSource = new ValueDataSource
                    {
                        QueueName = queue,
                        Namespace = m[i],
                        Name = "Pressure"
                    }
                });
                valueDefs.Add(new ConfigValueSpecification
                {
                    Name = $"{m[i]}:Temperature",
                    DataSource = new ValueDataSource
                    {
                        QueueName = queue,
                        Namespace = m[i],
                        Name = "Temperature"
                    }
                });

                valueActions.Add(new UiComponent
                {
                    Id = $"ValueDisplay{i + 1}a",
                    Type = ConfigConstants.Components.ValueMonitor,
                    Name = $"{m[i]}:NumRevolutions",
                    Tab = ConfigConstants.Tabs.Livedata
                });
                valueActions.Add(new UiComponent
                {
                    Id = $"ValueDisplay{i + 1}b",
                    Type = ConfigConstants.Components.ValueMonitor,
                    Name = $"{m[i]}:Pressure",
                    Tab = ConfigConstants.Tabs.Livedata
                });
                valueActions.Add(new UiComponent
                {
                    Id = $"ValueDisplay{i + 1}c",
                    Type = ConfigConstants.Components.GraphDisplay,
                    Name = $"{m[i]}:Temperature",
                    Tab = ConfigConstants.Tabs.Livedata
                });
            }

            conf.Actions = valueActions;
            conf.ValueDefinitions = valueDefs;

            var LiveDataGroup = new DeviceGroup
            {
                GroupName = GroupLiveData,
                VisibleTabs = new[] { ConfigConstants.Tabs.Livedata },
                Dashboard = new DashboardTabConfig
                {
                    Actions = valueActions.Select(x => x.Id).ToArray()
                },
                LiveFeed = new LiveFeedTabConfig()
                {
                    DataUpdateInterval = 500,
                    Values = valueDefs.Select(x => x.Name).ToArray()
                }
            };
            #endregion

            #region UIs
            var uiLayouts = new List<UiLayout>()
            {
                //No UIs
            };
            conf.Uis = uiLayouts;
            #endregion

            #region Groups

            conf.DeviceGroups = new[]
            {
                LiveDataGroup
            };

            #endregion




            //Generate Users
            var users = new List<User>();
            for (var i = 0; i < 4; i++)
            {
                users.Add(new User
                {
                    Username = $"operator{i+1}",
                    FullName = $"Operator {i+1}",
                    Groups = new[] { DefaultConfig.GroupChat },
                    Devices = new[]{ new SmartDevice
                        {
                            DeviceName = "A",
                            DeviceFamily = DeviceFamily.Phone,
                            DeviceType = DeviceType.Android,
                            DeviceGroups = new [] { GroupLiveData }
                        },
                        new SmartDevice
                        {
                            DeviceName = "B",
                            DeviceFamily = DeviceFamily.Watch,
                            DeviceType = DeviceType.Android,
                            DeviceGroups = null
                        },
                    }
                });
            }
            conf.Users = users;
            
            return conf;
        }
    }
}