//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Amqp;
using Amqp.Framing;
using Amqp.Types;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Todo;
using SmartDevicesGateway.Processing.Controller.SmartDevice;
using SmartDevicesGateway.TestCommon;
using SmartDevicesGateway.UnitTests;
using SmartDevicesGateway.UnitTests.Fixtures;
using Xunit;
using Xunit.Sdk;
using Trace = System.Diagnostics.Trace;

namespace SmartDevicesGateway.IntegrationTests.ruleengine
{
    [Trait("Requires", Traits.BROKER)]
    [Trait("Category", Categories.INTEGRATION)]
    public class TodoApiModelTest : IClassFixture<AmqpFixture>
    {
        public AmqpFixture Fixture { get; }

        public TodoApiModelTest(AmqpFixture fixture)
        {
            Fixture = fixture;
            Fixture.LoggerFactory.CreateLogger<ConfigServiceTest>();
            Model = new TodoListModel(Fixture.LoggerFactory, Fixture.AmqpService, Fixture.ConfigService);
        }

        public TodoListModel Model { get; set; }

        public string Domain { get; set; } = "TodoSample";
        public string User { get; set; } = "debug1.A";

        [Fact]
        public async Task GetListsTest()
        {
            var todoListHeaders = await Model.GetLists(Domain);
            Assert.NotNull(todoListHeaders);
            Assert.NotEmpty(todoListHeaders);
        }

        [Fact]
        public async Task GetListTest()
        {
            var todoListHeaders = (await Model.GetLists(Domain))?.ToList();
            Assert.NotNull(todoListHeaders);
            Assert.NotEmpty(todoListHeaders);

            foreach (var header in todoListHeaders)
            {
                var todoListDetails = await Model.GetList(header.Id, Domain);
                Assert.NotNull(todoListDetails);
                Assert.NotNull(todoListDetails.Header);
                Assert.NotNull(todoListDetails.Steps);
                Assert.NotEmpty(todoListDetails.Steps);
            }
        }

        [Fact]
        public async Task StartInstanceTest()
        {
            var todoListHeaders = (await Model.GetLists(Domain))?.ToList();
            Assert.NotNull(todoListHeaders);
            Assert.NotEmpty(todoListHeaders);

            var id = todoListHeaders.First().Id;

            var guid = await Model.StartInstance(id, Domain, User, null);
            Assert.NotEqual(Guid.Empty, guid);
        }

        [Fact]
        public async Task ManageSingleInstanceTest()
        {
            var todoListHeaders = (await Model.GetLists(Domain))?.ToList();
            Assert.NotNull(todoListHeaders);
            Assert.NotEmpty(todoListHeaders);

            var id = todoListHeaders.First().Id;

            var guid = await Model.StartInstance(id, Domain, User, null);
            Assert.NotEqual(Guid.Empty, guid);

            var instance = await Model.GetInstance(guid);
            Assert.NotNull(instance);
            Assert.NotNull(instance.Instance);

            foreach (var step in instance.Steps)
            {
                await Model.ConfirmStep(guid, step.Number, "test", true);
            }

            var ret = await Model.CloseInstance(guid, User);
            Assert.True(ret);
        }

        [Fact(Skip = "fix this in rule engine")]
        public async Task AbortInstanceTest()
        {
            var todoListHeaders = (await Model.GetLists(Domain))?.ToList();
            Assert.NotNull(todoListHeaders);
            Assert.NotEmpty(todoListHeaders);

            var id = todoListHeaders.First().Id;

            var guid = await Model.StartInstance(id, Domain, User, null);
            Assert.NotEqual(Guid.Empty, guid);

            var instance = await Model.GetInstance(guid);
            Assert.NotNull(instance);
            Assert.NotNull(instance.Instance);

            var step = instance.Steps.First();
            await Model.ConfirmStep(guid, step.Number, "test", true);

            var ret = await Model.AbortInstance(guid, User);
            Assert.True(ret);

            var instances = (await Model.GetInstances(Domain))?.ToList();
            Assert.NotNull(instances);
            Assert.Equal(0, instances.Count(x => x.Id == guid));
        }

        [Fact]
        public async Task GetInstancesTest()
        {
            var todoListInstanceHeaderDtos = await Model.GetInstances(Domain);
            Assert.NotNull(todoListInstanceHeaderDtos);
        }

        [Fact]
        public async Task ManageInstancesTest()
        {
            var todoListHeaders = (await Model.GetLists(Domain))?.ToList();
            Assert.NotNull(todoListHeaders);
            Assert.NotEmpty(todoListHeaders);

            //generate instances data
            var allGuids = new List<Guid>();
            var ids = todoListHeaders.GetRange(0, 2).Select(x => x.Id);
            foreach (var id in ids)
            {
                var guid = await Model.StartInstance(id, Domain, User, null);
                Assert.NotEqual(Guid.Empty, guid);
                allGuids.Add(guid);
            }
            
            //test get instances
            var instances = (await Model.GetInstances(Domain))?.ToList();
            Assert.NotNull(instances);
            Assert.NotEmpty(instances);

            var knownIds = instances.Select(x => x.Id);
            var count = allGuids.Count(x => knownIds.Contains(x));
            Assert.Equal(allGuids.Count(), count);

            //test get instance
            foreach (var guid in allGuids)
            {
                var inst = await Model.GetInstance(guid);
                Assert.NotNull(inst);
            }

            //test step confirm
            foreach (var guid in allGuids)
            {
                var inst = await Model.GetInstance(guid);
                var details = await Model.GetList(inst.Instance.Header.Id, Domain);
                
                foreach (var step in details.Steps)
                {
                    var res = await Model.ConfirmStep(guid, step.Number, User, true);
                    Assert.Equal(0, res); //TODO?
                }

                //close list
                var ret = await Model.CloseInstance(guid, User);
                Assert.True(ret);
            }
        }
    }
}
