using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Config.SDConfig.Tabs;
using SmartDevicesGateway.Model.Enums;
using SmartDevicesGateway.Model.Internal;
using SmartDevicesGateway.Model.Tabs;
using SmartDevicesGateway.Model.Ui;

namespace ConfigGenerator.Configs.General
{
    public class DefaultConfig : SmartDeviceConfigGenerator
    {
        public const string GroupChat = "Chat";

        [SuppressMessage("ReSharper", "InconsistentNaming")]
        public override SmartDeviceConfig GenerateConfig(SmartDeviceConfig conf)
        {
            conf.AppInfo = new AppInfo
            {
                Id = "Sample",
                Title = "SmartDevices",
                Theme = null,
                TitleResource = null,
                Subtitle = "Sample App",
            };

            //Generate TabConfig
            var statusFilter = new TabFilterEntry()
            {
                Key = "status",
                Name = "State",
                FilterType = "state",
                Invertable = true
            };

            var textFilter = new TabFilterEntry()
            {
                Key = "text",
                Name = "Text",
                FilterType = "text",
                Invertable = false
            };

            var titleFilter = new TabFilterEntry()
            {
                Key = "title",
                Name = "Titel",
                FilterType = "text",
                Invertable = false
            };

            var textSort = new TabSortEntry() {Key = "text", Name = "Text"};
            var titleSort = new TabSortEntry() { Key = "title", Name = "Titel" };
            var dateSort = new TabSortEntry() {Key = "createdate", Name = "Create Date"};
            var typeSort = new TabSortEntry() {Key = "type", Name = "Type"};
            var stateSort = new TabSortEntry() {Key = "state", Name = "State"};
            var nameSort = new TabSortEntry() { Key = "name", Name = "Name" };
            var idSort = new TabSortEntry() { Key = "id", Name = "Id" };

            conf.TabConfig = new List<TabConfig>()
            {
                new TabConfig()
                {
                    Title = "Live-Data",
                    Key = ConfigConstants.Tabs.Livedata,
                    MainTab = false,
                    Icon = AppDrawables.livefeed,
                    FilterEntries = new List<TabFilterEntry>(),
                    SortEntries = new List<TabSortEntry>{ nameSort, idSort }
                },
                new TabConfig()
                {
                    Title = "Jobs",
                    Key = ConfigConstants.Tabs.Dashboard,
                    MainTab = true,
                    ShowBadgeNumber = true,
                    Icon = AppDrawables.job,
                    FilterEntries = new List<TabFilterEntry>()
                    {
                        titleFilter,
                        textFilter,
                        statusFilter
                    },
                    SortEntries = new List<TabSortEntry>()
                    {
                        titleSort,
                        textSort,
                        dateSort,
                        typeSort,
                        stateSort
                    }
                },
                new TabConfig()
                {
                    Title = "Actions",
                    Key = ConfigConstants.Tabs.Actions,
                    MainTab = false,
                    Icon = AppDrawables.action,
                    FilterEntries = new List<TabFilterEntry>(),
                    SortEntries = new List<TabSortEntry>{ nameSort }
                }
            };

            //Generate Actions
            var ChatAction = new UiAction
            {
                Id = "ChatAction",
                Type = ConfigConstants.Components.GenericAction,
                Name = "Chat",
                JobKey = "ChatSendMessage",
                Tab = ConfigConstants.Tabs.Actions,
                AdditionalProperties = {{"Image", AppDrawables.notification.ToString()}}
            };
            conf.Actions = new[] {ChatAction};

            //Generate Groups
            var ChatGroup = new DeviceGroup
            {
                GroupName = GroupChat,
                Dashboard = new DashboardTabConfig
                {
                    Actions = new[] {ChatAction.Id}
                },
                VisibleTabs = new[] {ConfigConstants.Tabs.Actions, ConfigConstants.Tabs.Dashboard}
            };
            conf.DeviceGroups = new[] {ChatGroup};

            //Generate UiLayouts
            conf.Uis = new List<UiLayout>
            {
                new UiLayout()
                {
                    Id = "ChatSendMessage",
                    Title = "Nachricht senden",
                    Type = ConfigConstants.Views.JobView,
                    Elements = new List<UiComponent>
                    {
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.Spinner,
                            Id = "subject",
                            Name = "Senden an"
                        },
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.TextInput,
                            Id = "text",
                            Name = "Message"
                        },
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.DoubleButton,
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
                    Id = "ChatConfirmMessage",
                    Title = "Nachricht bestätigen",
                    Type = ConfigConstants.Views.JobView,
                    Elements = new List<UiComponent>
                    {
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.TextView,
                            Id = "subject",
                            Name = "Absender"
                        },
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.TextView,
                            Id = "text",
                            Name = "Message"
                        },
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.Button,
                            Id = "submit",
                            Name = "Bestätigen",
                            AdditionalProperties =
                            {
                                {"OnClick", "SendJob"},
                                {"Primary", true}
                            }
                        },
                    }
                }
            };

            //Generate Users
            var users = new List<User>();
            for (var i = 0; i < 4; i++)
            {
                users.Add(new User
                {
                    Username = $"chat{i}",
                    FullName = $"Chat-User {i}",
                    Groups = new[] { GroupChat },
                    Devices = new[]{ new SmartDevice
                        {
                            DeviceName = "A",
                            DeviceFamily = DeviceFamily.Phone,
                            DeviceType = DeviceType.Android,
                            DeviceGroups = new [] { ChatGroup.GroupName }
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