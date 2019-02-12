using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using ConfigGenerator.Configs.General;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Config.SDConfig.Tabs;
using SmartDevicesGateway.Model.Enums;
using SmartDevicesGateway.Model.Ui;
using static ConfigGenerator.ConfigConstants;

namespace ConfigGenerator.Configs.UseCases
{
    public class Uc1ToolBreakage : SmartDeviceConfigGenerator
    {
        public const string ReportToolBreakageAction = "ReportToolBreakage";


        [SuppressMessage("ReSharper", "InconsistentNaming")]
        public override SmartDeviceConfig GenerateConfig(SmartDeviceConfig conf)
        {
            //Generate Actions
            var ReportToolBreakageAction = new UiAction
            {
                Id = "ReportToolBreakage",
                Type = Components.GenericAction,
                Name = "Report tool breakage",
                JobKey = "ToolBreakage",
                Tab = Tabs.Actions,
                AdditionalProperties = {{"Image", AppDrawables.broken.ToString()}}
            };
            conf.Actions = new[] { ReportToolBreakageAction };

            //Generate Groups
            var WorkerGroup = new DeviceGroup
            {
                GroupName = "Operators",
                VisibleTabs = new[] { Tabs.Dashboard },
                Dashboard = new DashboardTabConfig { Actions = new []{ ReportToolBreakageAction.Id }}
            };
            var LogisticGroup = new DeviceGroup
            {
                GroupName = "Logistics",
                VisibleTabs = new[] { Tabs.Dashboard }
            };
            var MaintenanceGroup = new DeviceGroup
            {
                GroupName = "Maintenance",
                VisibleTabs = new[] { Tabs.Dashboard }
            };
            conf.DeviceGroups = new[] { WorkerGroup, LogisticGroup, MaintenanceGroup };

            //Define UI-Elements
            var SendCancelDoubleButton = new UiComponent
            {
                Type = Components.DoubleButton,
                Id = "submit",
                Name = "Senden",
                AdditionalProperties =
                {
                    {"TextSecondary", "Abbrechen"},
                    {"OnClickPrimary", "SendJob"},
                    {"OnClickSecondary", "RemoveJob"},
                    {"Primary", true}
                }
            };

            //Generate UI-Layouts
            conf.Uis = new List<UiLayout>
            {
                new UiLayout()
                {
                    Id = "ChatSendMessage",
                    Title = "Nachricht senden",
                    Type = Views.JobView,
                    Elements = new List<UiComponent>
                    {
                        new UiComponent
                        {
                            Type = Components.Spinner,
                            Id = "subject",
                            Name = "Senden an"
                        },
                        new UiComponent
                        {
                            Type = Components.TextInput,
                            Id = "text",
                            Name = "Message"
                        },
                        new UiComponent
                        {
                            Type = Components.DoubleButton,
                            Id = "submit",
                            Name = "Bearbeiten",
                            AdditionalProperties =
                            {
                                {"TextSecondary", "Abbrechen"},
                                {"OnClickPrimary", "SendJob"},
                                {"OnClickSecondary", "RemoveJob"},
                                {"Primary", true}
                            }
                        },
                    },
                    AdditionalProperties = { { "OnBackPressed", "RemoveJob" } }
                },
                new UiLayout()
                {
                    Id = "ToolBreakage",
                    Title = "Report tool breakage",
                    Type = Views.JobView,
                    Elements = new List<UiComponent>
                    {
                         new UiComponent
                        {
                            Type = Components.Spinner,
                            Id = "subject",
                            Name = "Maschine / Location"
                        },
                        new UiComponent
                        {
                            Type = Components.TextInput,
                            Id = "text",
                            Name = "Article / Number"
                        },
                        new UiComponent
                        {
                            Type = Components.TextInput,
                            Id = "description",
                            Name = "Description",
                            AdditionalProperties = {{"InputType", "multiline"}}
                        },
                        new UiComponent
                        {
                            Type = Components.Switch,
                            Id = "helpRequested",
                            Name = "Needs assessment from Maintenance Team?"
                        },
                        new UiComponent
                        {
                            Type = Components.NumberInput,
                            Id = "timeoffset",
                            Name = "Tool is disasselbled in ...",
                            AdditionalProperties =
                            {
                                {"Interval", 5},
                                {"Count", 25},
                                {"Suffix", " Minutes"}
                            }
                        },
                        new UiComponent
                        {
                            Type = Components.Button,
                            Id = "submit",
                            Name = "Bestätigen",
                            AdditionalProperties =
                            {
                                {"OnClick", "SendJob"},
                                {"Primary", true}
                            }
                        },
                        SendCancelDoubleButton
                    }
                }
                //TODO: Add all remaining UIs
            };

            //Generate Users
            var users = new List<User>();
            for (var i = 0; i < 4; i++)
            {
                users.Add(new User
                {
                    Username = $"operator{i}",
                    FullName = $"Operator {i}",
                    Groups = new[] { WorkerGroup.GroupName },
                    Devices = new []{ new SmartDevice
                        {
                            DeviceName = "A",
                            DeviceFamily = DeviceFamily.Phone,
                            DeviceType = DeviceType.Android,
                            DeviceGroups = new [] { DefaultConfig.GroupChat }
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
            for (var i = 0; i < 4; i++)
            {
                users.Add(new User
                {
                    Username = $"log{i}",
                    FullName = $"Logistician {i}",
                    Groups = new[] { WorkerGroup.GroupName },
                    Devices = new[]{ new SmartDevice
                        {
                            DeviceName = "A",
                            DeviceFamily = DeviceFamily.Phone,
                            DeviceType = DeviceType.Android,
                            DeviceGroups = new [] { DefaultConfig.GroupChat }
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
            for (var i = 0; i < 4; i++)
            {
                users.Add(new User
                {
                    Username = $"maintenance{i}",
                    FullName = $"Maintainer {i}",
                    Groups = new[] { WorkerGroup.GroupName },
                    Devices = new[]{ new SmartDevice
                        {
                            DeviceName = "A",
                            DeviceFamily = DeviceFamily.Phone,
                            DeviceType = DeviceType.Android,
                            DeviceGroups = new [] { DefaultConfig.GroupChat }
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