//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json.Linq;
using SmartDevicesGateway.Model.Todo;
using SmartDevicesGateway.Processing.Controller.Base;
using SmartDevicesGateway.Processing.Controller.Util;
using SmartDevicesGateway.Services.ConfigService;
using Vogler.Amqp;

namespace SmartDevicesGateway.Processing.Controller.SmartDevice
{
    public class TodoListModel : AbstractModel
    {
        private readonly IAmqpService _amqpService;
        private readonly IConfigService _configService;

        //public AmqpServiceConfig AmqpServiceConfig
        //{
        //    get => _amqpServiceConfig;
        //    set
        //    {
        //        _amqpServiceConfig = value;
        //        RequestQueue = RequestQueue.
        //    }
        //}

        private string RequestQueue { get; set; }

        private string LinkServiceName { get; set; } = "todo";

        public TodoListModel(ILoggerFactory loggerFactory, IAmqpService amqpService, IConfigService configService) :
            base(loggerFactory)
        {
            _amqpService = amqpService;
            _configService = configService;

            RequestQueue = "ToDo";
            //TODO add RequestQueue to ServiceConfig
            //var configProvider = _configService.GetProvider<ServiceConfig>();
            //AmqpServiceConfig = configProvider.Get()?.AmqpServiceConfig;
            //configProvider.ConfigChangedEvent += (sender, args) =>
            //{
            //    AmqpServiceConfig = args.NewValue?.AmqpServiceConfig;
            //};
        }

        public virtual Task<IEnumerable<TodoListHeader>> GetLists(string contextDomain)
        {
            var requestBody = new
            {
                context = new { },
                domain = contextDomain
            };

            var jResult = _amqpService.RequestReply(requestBody, "ToDoFind", RequestQueue);
            var response = AmqpApiUtils.ParseApiResult(jResult);

            if (!response.ResponseObject.ContainsKey("headers"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var todoListHeaders = response.ResponseObject["headers"].ToObject<TodoListHeader[]>().AsEnumerable();

            return Task.FromResult(todoListHeaders);
        }

        public virtual Task<TodoListDetails> GetList(string listId, string contextDomain)
        {
            var requestBody = new
            {
                id = listId,
                domain = contextDomain
            };

            var jResult = _amqpService.RequestReply(requestBody, "ToDoGetDetails", RequestQueue);
            var response = AmqpApiUtils.ParseApiResult(jResult);

            if (!response.ResponseObject.ContainsKey("header"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var todoListHeader = response.ResponseObject["header"].ToObject<TodoListHeader>();

            if (!response.ResponseObject.ContainsKey("steps"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var todoListSteps = response.ResponseObject["steps"].ToObject<TodoApiStep[]>();
            var steps2 = todoListSteps.Select(x => new TodoListStep
            {
                //TODO rename properties in app
                Description = x.Description,
                Name = x.Name,
                Number = x.Step,
                Resource = x.Resource
            }).ToList();

            return Task.FromResult(new TodoListDetails
            {
                Header = todoListHeader,
                Steps = steps2
            });
        }

        public virtual Task<IEnumerable<TodoListInstanceHeaderDto>> GetInstances(string contextDomain)
        {
            var requestBody = new
            {
                context = new { },
                domain = contextDomain
            };

            var jResult = _amqpService.RequestReply(requestBody, "ToDoGetRunningInstances", RequestQueue);
            var response = AmqpApiUtils.ParseApiResult(jResult);

            if (!response.ResponseObject.ContainsKey("instances"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var instances = response.ResponseObject["instances"].ToObject<TodoListInstanceHeaderDto[]>().AsEnumerable();
            return Task.FromResult(instances);
        }

        public virtual Task<TodoListInstanceDto> GetInstance(Guid id)
        {
            var requestBody = new
            {
                instanceId = id.ToString()
            };
            var jResult = _amqpService.RequestReply(requestBody, "ToDoGetInstanceDetails", RequestQueue);
            var response = AmqpApiUtils.ParseApiResult(jResult);

            if (!response.ResponseObject.ContainsKey("instance"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var instance = response.ResponseObject["instance"].ToObject<TodoListInstanceHeaderDto>();

            if (!response.ResponseObject.ContainsKey("closedSteps"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var closedStep = response.ResponseObject["closedSteps"].ToObject<TodoListClosedStep[]>();

            if (!response.ResponseObject.ContainsKey("steps"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var steps = response.ResponseObject["steps"].ToObject<TodoApiStep[]>();
            var steps2 = steps.Select(x => new TodoListStep
            {
                //TODO rename properties in app
                Description = x.Description,
                Name = x.Name,
                Number = x.Step,
                Resource = x.Resource
            }).ToList();

            return Task.FromResult(new TodoListInstanceDto
            {
                Instance = instance,
                ClosedSteps = closedStep,
                Steps = steps2
            });
        }

        public virtual Task<Guid> StartInstance(string todoListId, string contextDomain, string startedByUser,
            JObject context)
        {
            var requestBody = new
            {
                context,
                definitionId = todoListId,
                domain = contextDomain,
                startedBy = startedByUser
            };

            var jResult = _amqpService.RequestReply(requestBody, "ToDoStartInstance", RequestQueue);
            var response = AmqpApiUtils.ParseApiResult(jResult);

            if (!response.ResponseObject.ContainsKey("instanceId"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var instanceId = response.ResponseObject["instanceId"].ToObject<Guid>();

            return Task.FromResult(instanceId);
        }

        public virtual async Task<int> ConfirmStep(Guid instId, int stepNum, string closedByUser, bool state)
        {
            if (state) //close step
            {
                await MarkStepClosed(instId, stepNum, closedByUser);
            }
            else //open step
            {
                await MarkStepOpened(instId, stepNum, closedByUser);
            }

            return 0; //TODO
        }

        public virtual Task<Guid> MarkStepClosed(Guid instId, int stepNum, string closedByUser)
        {
            var requestBody = new
            {
                closedBy = closedByUser,
                instanceId = instId.ToString(),
                step = stepNum
            };

            var jResult = _amqpService.RequestReply(requestBody, "ToDoMarkStepClosed", RequestQueue);
            var response = AmqpApiUtils.ParseApiResult(jResult);

            if (!response.ResponseObject.ContainsKey("stepId"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var stepId = response.ResponseObject["stepId"].ToObject<Guid>();

            return Task.FromResult(stepId);
        }

        public virtual async Task<bool> MarkStepOpened(Guid instId, int stepNum, string closedByUser)
        {
            var instance = await GetInstance(instId);
            var closedStepId = instance.ClosedSteps.FirstOrDefault(x => x.Step == stepNum)?.Uuid;

            var requestBody = new
            {
                stepId = closedStepId,
                valid = true
            };

            var jResult = _amqpService.RequestReply(requestBody, "ToDoMarkStepOpen", RequestQueue, null, true);

            //TODO fix this in API
            if (jResult == null)
            {
                return true;
            }

            var response = AmqpApiUtils.ParseApiResult(jResult);

            if (!response.ResponseObject.ContainsKey("stepId"))
            {
                throw new AmqpApiException("Could not read ApiResponse");
            }

            var stepId = response.ResponseObject["stepId"].ToObject<Guid>();

            return true;
        }

        public virtual Task<bool> CloseInstance(Guid guid, string deviceId)
        {
            var requestBody = new
            {
                closedBy = deviceId,
                instanceId = guid.ToString()
            };

            try
            {
                var jResult = _amqpService.RequestReply(requestBody, "ToDoCloseInstance", RequestQueue);
            }
            catch(Exception e)
            {
                Logger.LogWarning(e, "Exception in CloseInstance (Workaraound for empty message from RuleEngine)");
            }
//            var response = ParseApiResult(jResult);
//
//            if (!response.ResponseObject.ContainsKey("instanceId"))
//            {
//                throw new AmqpApiException("Could not read ApiResponse");
//            }
//            var instanceId = response.ResponseObject["instance"].ToObject<Guid>();

//            return response?.Error == null;
            return Task.FromResult(true);
        }

        public virtual Task<bool> AbortInstance(Guid guid, string deviceId)
        {
            var requestBody = new
            {
                abortedBy = deviceId,
                instanceId = guid.ToString()
            };

            try
            {
                var jResult = _amqpService.RequestReply(requestBody, "ToDoAbortInstance", RequestQueue);
            }
            catch (Exception e)
            {
                Logger.LogWarning(e, "Exception in CloseInstance (Workaraound for empty message from RuleEngine)");
            }
            //            var response = ParseApiResult(jResult);
            //
            //            if (!response.ResponseObject.ContainsKey("instanceId"))
            //            {
            //                throw new AmqpApiException("Could not read ApiResponse");
            //            }
            //            var instanceId = response.ResponseObject["instance"].ToObject<Guid>();

            //            return response?.Error == null;
            return Task.FromResult(true);
        }
    }

    public class TodoApiStep
    {
        public string Name { get; set; }
        public string Description { get; set; }
        public int Step { get; set; }
        public Uri Resource { get; set; }
    }
}