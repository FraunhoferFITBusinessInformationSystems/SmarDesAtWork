//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json.Linq;
using SmartDevicesGateway.Model.Dto.Config;

namespace SmartDevicesGateway.Model.Todo
{
    public class TodoListInstance
    {
        public Guid Id { get; set; }
        public string TodoListId { get; set; }

        public Dictionary<int, bool> StepState { get; set; } = new Dictionary<int, bool>();

        public int CurrentStep { get; set; }
    }

    public class TodoListDetails
    {
        public TodoListHeader Header { get; set; }
        public IList<TodoListStep> Steps { get; set; }
    }

    public class TodoListStep
    {
        public const int StepIdNoMoreSteps = -1;

        public int Number { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }

        public Uri Resource { get; set; }
    }

    public class TodoListClosedStep
    {
        public DateTimeOffset ClosedAt { get; set; }
        public string ClosedBy { get; set; }
        public int Step { get; set; }
        public Guid Uuid { get; set; }
    }

    public class TodoListHeader
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
    }

    public class TodoListInstanceDto
    {
        public TodoListInstanceHeaderDto Instance { get; set; }
        public IList<TodoListClosedStep> ClosedSteps { get; set; }
        public IList<TodoListStep> Steps { get; set; }
    }

    public class TodoListInstanceHeaderDto
    {
        public Guid Id { get; set; }
        public TodoListHeader Header { get; set; }
        public DateTimeOffset StartedAt { get; set; }
        public string StartedBy { get; set; }
        public JObject Context { get; set; }
    }
}
