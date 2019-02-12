using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Config.SDConfig.Tabs;
using SmartDevicesGateway.Model.Enums;
using SmartDevicesGateway.Model.Ui;

namespace ConfigGenerator.Configs.General
{
    public class TodoConfig : SmartDeviceConfigGenerator
    {
        public const string TodoListStartAction = "TodoListStartAction";
        public const string TodoListBarcodeAction = "TodoListBarcodeAction";
        public const string TodoListPickupAction = "TodoListPickupAction";

        [SuppressMessage("ReSharper", "InconsistentNaming")]
        public override SmartDeviceConfig GenerateConfig(SmartDeviceConfig conf)
        {
            conf.Actions = new[] {
                new UiAction
                {
                    Id = TodoListStartAction,
                    Type = ConfigConstants.Components.GenericAction,
                    Name = "Neue TODO-Liste starten",
                    JobKey = "TodoListStart",
                    Tab = ConfigConstants.Tabs.Actions,
                    AdditionalProperties = { { "Image", AppDrawables.list_add.ToString() } }
                },
                new UiAction
                {
                    Id = TodoListBarcodeAction,
                    Type = ConfigConstants.Components.GenericAction,
                    Name = "TODO-Liste: Auftrag scannen",
                    JobKey = "TodoListBarcodeStart",
                    Tab = ConfigConstants.Tabs.Actions,
                    AdditionalProperties = { { "Image", AppDrawables.barcode.ToString() } }
                },
                new UiAction
                {
                    Id = TodoListPickupAction,
                    Type = ConfigConstants.Components.GenericAction,
                    Name = "TODO-Liste weiter bearbeiten",
                    JobKey = "TodoListPickup",
                    Tab = ConfigConstants.Tabs.Actions,
                    AdditionalProperties = { { "Image", AppDrawables.list_play.ToString() } }
                }
            };

            conf.Uis = new List<UiLayout>
            {
                new UiLayout()
                {
                    Id = "TodoListPickup",
                    Title = "TODO-Liste weiter bearbeiten",
                    Type = ConfigConstants.Views.JobView,
                    Elements = new List<UiComponent>
                    {
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.Spinner,
                            Id = "subject",
                            Name = "Liste"
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
                    }
                },
                new UiLayout()
                {
                    Id = "TodoListBarcodeStart",
                    Title = "Todo-Liste: Auftrag scannen",
                    Type = ConfigConstants.Views.JobView,
                    Elements = new List<UiComponent>
                    {
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.BarcodeInput,
                            Id = "subject",
                            Name = "Barcode"
                        },
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.TextInput,
                            Id = "number",
                            Name = "Auftragsnummer"
                        },
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.TextInput,
                            Id = "notes",
                            Name = "weitere Informationen"
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
                    }
                },
                new UiLayout()
                {
                    Id = "TodoListStart",
                    Title = "TODO-Liste Starten",
                    Type = ConfigConstants.Views.JobView,
                    Elements = new List<UiComponent>
                    {
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.Spinner,
                            Id = "subject",
                            Name = "Liste"
                        },
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.TextInput,
                            Id = "number",
                            Name = "Auftragsnummer"
                        },
                        new UiComponent
                        {
                            Type = ConfigConstants.Components.TextInput,
                            Id = "notes",
                            Name = "weitere Informationen"
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
                    }
                },
                new UiLayout()
                {
                    Id = "GenericTodoList",
                    Title = "TODO-Liste",
                    Type = ConfigConstants.Views.TodoList,
                    AdditionalProperties =
                    {
                        {"CanDelete", true},
                        {"CanForward", true},
                    }
                }
            };

            var TodoEditorGroup = new DeviceGroup
            {
                GroupName = "TodoEditor",
                Dashboard = new DashboardTabConfig
                {
                    Actions = new[] {TodoListStartAction, TodoListBarcodeAction, TodoListPickupAction}
                },
                VisibleTabs = new[] {ConfigConstants.Tabs.Actions, ConfigConstants.Tabs.Dashboard}
            };
            conf.DeviceGroups = new List<DeviceGroup>{ TodoEditorGroup };
            return conf;
        }
    }
}
