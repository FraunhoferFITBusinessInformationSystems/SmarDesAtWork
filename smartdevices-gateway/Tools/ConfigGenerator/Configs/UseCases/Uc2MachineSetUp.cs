using System.Diagnostics.CodeAnalysis;

namespace ConfigGenerator.Configs.UseCases
{
    public class Uc2MachineSetUp : SmartDeviceConfigGenerator
    {
        [SuppressMessage("ReSharper", "InconsistentNaming")]
        public override SmartDeviceConfig GenerateConfig(SmartDeviceConfig conf)
        {
//            //Generate Actions
//            var ChatAction = new UiAction
//            {
//            };
//            Actions = new[] {ChatAction};
//
//            //Generate Groups
//            var ChatGroup = new DeviceGroup
//            {
//            };
//            DeviceGroups = new[] {ChatGroup};
//
//            //Generate UiLayouts
//            Uis = new List<UiLayout>
//            {
//                new UiLayout()
//                {
//                }
//            };
//
//            //Generate Users
//            var users = new List<User>();
//            for (var i = 0; i < 4; i++)
//            {
//                users.Add(new User
//                {
//                    Username = $"debug{i}",
//                    FullName = $"Debug {i}",
//                    Groups = new[] { WorkerGroup.GroupName },
//                    Devices = new []{ new SmartDevice
//                        {
//                            DeviceName = "A",
//                            DeviceFamily = DeviceFamily.Phone,
//                            DeviceType = DeviceType.Android,
//                            DeviceGroups = new [] { ChatGroup.GroupName, TodoEditorGroup.GroupName }
//                        },
//                        new SmartDevice
//                        {
//                            DeviceName = "B",
//                            DeviceFamily = DeviceFamily.Watch,
//                            DeviceType = DeviceType.Android,
//                            DeviceGroups = null
//                        },
//                    }
//                });
//            }
//            Users = users;

            return conf;
        }
    }
}