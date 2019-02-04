//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json.Linq;
using SmartDevicesGateway.Model.Dto.Config;
using SmartDevicesGateway.Model.Todo;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Processing.Exceptions;
using SmartDevicesGateway.Processing.Handler;
using SmartDevicesGateway.Services.ConfigService;
using Vogler.Amqp;

namespace SmartDevicesGateway.Processing.Controller.SmartDevice
{
    public class TodoListModelMock : TodoListModel
    {
        private readonly TodoListHandler _todoListHandler;

        private IEnumerable<TodoListDetails> TodoLists { get; } = new List<TodoListDetails>()
        {
            new TodoListDetails()
            {
                Header   = new TodoListHeader()
                {
                    Id = "Primadonna1",
                    Name = "Delonghi Primadonna: Inspektion",
                    Description = "Gehäuse öffnen, Sichtprüfung, Gehäuse schließen"
                },
                Steps = new List<TodoListStep>()
                {
                    new TodoListStep()
                    {
                        Number = 10,
                        Name = "Schrauben lösen",
                        Description = "Die 4 äußeren Gehäuseschrauben lösen."
                    },
                    new TodoListStep()
                    {
                        Number = 20,
                        Name = "Seitenwand öffnen",
                        Description = "Linke Seitenwand etwas zurückziehen...unten herausschwenken und abnehmen."
                    },
                    new TodoListStep()
                    {
                        Number = 30,
                        Name = "Tank entnehmen",
                        Description = "Tank entnehmen und mit der 2. Seitenwand genauso vorgehen."
                    },
                    new TodoListStep()
                    {
                        Number = 40,
                        Name = "Rückwand entfernen",
                        Description = "Die 2 Schrauben der Rückwand entfernen und diese abnehmen."
                    }
                }
            },
            new TodoListDetails()
            {
                Header   = new TodoListHeader()
                {
                    Id = "Primadonna2",
                    Name = "Delonghi Primadonna: Entkalkung",
                    Description = "Entkalkung der Kaffemaschine"
                },
                Steps = new List<TodoListStep>()
                {
                    new TodoListStep()
                    {
                        Number = 10,
                        Name = "Entkalker einfüllen",
                        Description = "Entkalker mit 1l Wasser in Behälter füllen."
                    },
                    new TodoListStep()
                    {
                        Number = 20,
                        Name = "Reinigung starten",
                        Description = "Wassertaste 5 Sekunden drücken, bis LED leuchtet."
                    },
                    new TodoListStep()
                    {
                        Number = 30,
                        Name = "Dampfspülung",
                        Description = "Dampfdrehknopf aufdrehen, 30 Minuten warten."
                    },
                    new TodoListStep()
                    {
                        Number = 40,
                        Name = "Frischwasser nachfüllen",
                        Description = "Wenn Wasserlampe leuchtet, Dampfdrehknopf zudrehen, Behälter mit Frischwasser füllen."
                    },
                    new TodoListStep()
                    {
                        Number = 50,
                        Name = "Dampfspühlung",
                        Description = "Dampfdrehknopf aufdrehen, 5 Minuten warten, bis Wasserlampe leuchtet."
                    },
                    new TodoListStep()
                    {
                        Number = 60,
                        Name = "Frischwasser nachfüllen",
                        Description = "Behälter mit Frischwasser füllen."
                    },
                    new TodoListStep()
                    {
                        Number = 70,
                        Name = "Nachspülen",
                        Description = "Wassertaste dreimal drücken."
                    }
                }
            }
        };

        public TodoListModelMock(ILoggerFactory loggerFactory,
            TodoListHandler todoListHandler, IAmqpService amqpService, IConfigService configService) : base(loggerFactory, amqpService, configService)
        {
            _todoListHandler = todoListHandler;
            if (_todoListHandler.Instances.Count == 0)
            {
                StartInstance("Primadonna2", "TodoSample", "debug1.A", Guid.Parse("f65d2135-a3d8-4446-b767-4aaaafae8b7a"));
                StartInstance("Primadonna1", "TodoSample", "debug1.A", Guid.Parse("f65d2135-a3d8-4446-b767-4aaaafae8b7b"));
            }
        }

        public override Task<IEnumerable<TodoListHeader>> GetLists(string contextDomain)
        {
            return Task.FromResult(
                (IEnumerable<TodoListHeader>) TodoLists.Select(x => x.Header).ToList()
            );
        }

        public override Task<TodoListDetails> GetList(string listId, string contextDomain)
        {
            return Task.FromResult(
                TodoLists.FirstOrDefault(x => string.Equals(x.Header.Id, listId, StringComparison.Ordinal))
            );
        }

        public override Task<IEnumerable<TodoListInstanceHeaderDto>> GetInstances(string contextDomain)
        {
            var todoListInstances = _todoListHandler.Instances.ToList();
            var todoListInstanceHeaderDtos = todoListInstances.Select(x =>
                new TodoListInstanceHeaderDto()
                {
                    Id = x.Id,
                    Header = TodoLists.FirstOrDefault(y => y.Header.Id == x.TodoListId)?.Header
                }).ToList();
            return Task.FromResult((IEnumerable<TodoListInstanceHeaderDto>)todoListInstanceHeaderDtos);
        }

        public override Task<TodoListInstanceDto> GetInstance(Guid id)
        {
            var todoListInstance = _todoListHandler.Instances.FirstOrDefault(x => x.Id == id);
            if (todoListInstance == null)
            {
                return null;
            }

            var todoListDetails = TodoLists.FirstOrDefault(x => x.Header.Id == todoListInstance.TodoListId);

            return Task.FromResult(new TodoListInstanceDto()
            {
                Steps = todoListDetails.Steps,
                Instance = new TodoListInstanceHeaderDto()
                {
                    Header = TodoLists.FirstOrDefault(x =>
                        string.Equals(x.Header.Id, todoListInstance.TodoListId, StringComparison.Ordinal))?.Header,
                    Id = todoListInstance.Id
                },
                ClosedSteps = todoListInstance.StepState.Select(x => new TodoListClosedStep
                {
                    ClosedAt = DateTimeOffset.Now,
                    ClosedBy = "debug1.A",
                    Step = x.Key,
                    Uuid = Guid.NewGuid()
                }).ToList()
            });
        }

        public override Task<Guid> StartInstance(string todoListId, string contextDomain, string startedBy, JObject context)
        {
            return StartInstance(todoListId, contextDomain, startedBy, Guid.NewGuid());
        }

        public Task<Guid> StartInstance(string todoListId, string contextDomain, string startedBy, Guid id)
        {
            var todoListDetails = TodoLists.FirstOrDefault(x =>
                string.Equals(x.Header.Id, todoListId, StringComparison.Ordinal));
            if (todoListDetails == null)
            {
                throw new UnknownKeyException("Unknown TodoListId");
            }

            var firstStep = todoListDetails.Steps.OrderBy(x => x.Number).FirstOrDefault();
            var instance = new TodoListInstance()
            {
                Id = (id == Guid.Empty) ? Guid.NewGuid() : id,
                TodoListId = todoListId,
                CurrentStep = firstStep?.Number ?? 0
            };

            _todoListHandler.Instances.Add(instance);
            return Task.FromResult(instance.Id);
        }

        public override Task<int> ConfirmStep(Guid id, int step, string user, bool state)
        {
            var instance = _todoListHandler.Instances.FirstOrDefault(x => x.Id == id);
            if (instance == null)
            {
                throw new UnknownKeyException("Unknown instance id");
            }

            var list = TodoLists.FirstOrDefault(x =>
                string.Equals(x.Header.Id, instance.TodoListId, StringComparison.Ordinal));
            if (list == null)
            {
                throw new UnknownKeyException("Unknown TodoListId in instance");
            }

            if (state)
            {
                var nextStep = list.Steps.OrderBy(x => x.Number).FirstOrDefault(x => x.Number > instance.CurrentStep);
                var nextStepId = nextStep?.Number ?? TodoListStep.StepIdNoMoreSteps;
                instance.CurrentStep = nextStepId;
            }
            
            instance.StepState[step] = state;
            return Task.FromResult(instance.CurrentStep);
        }
    }
}
