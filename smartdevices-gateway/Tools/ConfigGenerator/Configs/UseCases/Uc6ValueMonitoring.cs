using System;
using System.Collections.Generic;
using System.Text;
using ConfigGenerator.Configs.General;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Enums;
using SmartDevicesGateway.Model.Ui;
using static ConfigGenerator.ConfigConstants;

namespace ConfigGenerator.Configs.UseCases
{
    public class Uc6ValueMonitoring : SmartDeviceConfigGenerator
    {
        public const string GroupValueMonitoring = "ValueMonitor";
        public const string JobLimitViolation = "UC6LimitViolation";
        
        public override SmartDeviceConfig GenerateConfig(SmartDeviceConfig conf)
        {

            #region ComponentDefinitions

            var EquipmentView = new UiComponent
            {
                Type = ConfigConstants.Components.TextView,
                Id = "equipment",
                Name = "Equipment"
            };
            var TextView = new UiComponent
            {
                Type = ConfigConstants.Components.TextView,
                Id = "text",
                Name = "Text"
            };
            var ActualView = new UiComponent
            {
                Type = ConfigConstants.Components.TextView,
                Id = "actual_value",
                Name = "Actual Value"
            };
            var SetPValue = new UiComponent
            {
                Type = ConfigConstants.Components.TextView,
                Id = "temperature_setp",
                Name = "Set Point Value"
            };
            var HHView = new UiComponent
            {
                Type = ConfigConstants.Components.TextView,
                Id = "temperature_HH",
                Name = "Upper Error Value"
            };
            var HView = new UiComponent
            {
                Type = ConfigConstants.Components.TextView,
                Id = "temperature_H",
                Name = "Upper Warn Value"
            };
            var LView = new UiComponent
            {
                Type = ConfigConstants.Components.TextView,
                Id = "temperature_L",
                Name = "Lower Warn Value"
            };
            var LLView = new UiComponent
            {
                Type = ConfigConstants.Components.TextView,
                Id = "temperature_LL",
                Name = "Lower Error Value"
            };
            var DateView = new UiComponent
            {
                Type = ConfigConstants.Components.DateView,
                Id = "time",
                Name = "Time"
            };
            var AcceptDeclineButton = new UiComponent
            {
                Type = ConfigConstants.Components.DoubleButton,
                Id = "submit",
                Name = "Accept",
                AdditionalProperties =
                {
                    {"Value", "Accept"},
                    {"OnClickPrimary", "SendJob"},
                    {"TextSecondary", "Decline"},
                    {"ValueSecondary", "Decline"},
                    {"OnClickSecondary", "SendJob"},
                    {"Primary", true}
                }
            };
            #endregion

            #region UIs
            var uiLayouts = new List<UiLayout>()
            {
                new UiLayout
                {
                    Id = JobLimitViolation,
                    Title = "Limit Violation",
                    Type = ConfigConstants.Views.JobView,
                    Elements = new []
                    {
                        EquipmentView,
                        TextView,
                        DateView,
                        ActualView,
                        HHView,
                        HView,
                        SetPValue,
                        LView,
                        LLView,
                        AcceptDeclineButton
                    }
                },
            };
            conf.Uis = uiLayouts;
            #endregion

            #region Groups

            conf.DeviceGroups = new[]
            {
                new DeviceGroup
                {
                    GroupName = GroupValueMonitoring,
                    VisibleTabs = new[] { Tabs.Dashboard },
                }
            };

            #endregion

            #region Users

            //Generate Users
            var users = new List<User>();
            for (var i = 0; i < 2; i++)
            {
                users.Add(new User
                {
                    Username = $"user{i + 1}",
                    FullName = $"User {i + 1}",
                    Groups = new[] { DefaultConfig.GroupChat, GroupValueMonitoring },
                    Devices = new[]{ new SmartDevice
                        {
                            DeviceName = "A",
                            DeviceFamily = DeviceFamily.Phone,
                            DeviceType = DeviceType.Android,
                            DeviceGroups = new [] { Uc5LiveData.GroupLiveData }
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

            #endregion

            return conf;
        }

    }
}
