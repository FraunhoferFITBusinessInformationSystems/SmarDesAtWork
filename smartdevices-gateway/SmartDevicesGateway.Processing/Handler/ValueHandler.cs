//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using Amqp;
using Amqp.Framing;
using Amqp.Types;
using Microsoft.Extensions.Logging;
using SmartDevicesGateway.Model.Amqp;
using SmartDevicesGateway.Model.Config.SDConfig;
using SmartDevicesGateway.Model.Config.ValueSpecifications;
using SmartDevicesGateway.Model.Persistence;
using SmartDevicesGateway.Model.Values;
using SmartDevicesGateway.Processing.AmqpParser;
using SmartDevicesGateway.Processing.Exceptions;
using SmartDevicesGateway.Services.ConfigService;
using Vogler.Amqp;

namespace SmartDevicesGateway.Processing.Handler
{
    public class ValueHandler : AbstractDataHandler<Value>
    {
        private readonly IPersistenceProvider _persistanceProvider;
        private readonly IAmqpService _amqpService;
        private readonly ILogger _logger;

        private readonly Dictionary<string, ValueDataSource> _dataSources = new Dictionary<string, ValueDataSource>();
        private readonly Dictionary<string, AmqpListener> _listeners = new Dictionary<string, AmqpListener>();

        public ValueHandler(ILoggerFactory loggerFactory,
            IPersistenceProvider persistanceProvider, IAmqpService amqpService) : base(loggerFactory)
        {
            _logger = loggerFactory.CreateLogger<ValueHandler>();
            _persistanceProvider = persistanceProvider;
            _amqpService = amqpService;
        }

        public IEnumerable<Value> GetValues(IEnumerable<string> valueNameIds)
        {
            return valueNameIds.Select<string, Value>(configValue => _persistanceProvider.GetValue(configValue))
                .ToList();
        }

        public Value GetValue(string valueNameId)
        {
            return _persistanceProvider.GetValue(valueNameId);
        }

        public void SetValue(string valueName, object obj)
        {
            _persistanceProvider.AddValue(valueName, new Value
            {
                Name = valueName,
                Val = obj
            });
        }

        public void SetValue(Value value)
        {
            _persistanceProvider.AddValue(value.Name, value);
        }

        public void AddDataSource(ValueDataSource dataSource)
        {
            if (!_dataSources.Values.Any(x => x.QueueName.Equals(dataSource.QueueName)))
            {
                //No Listeners on this queue so far, start Listening...
                _logger.LogInformation("Listening for ValueData on Queue \"{0}\"", dataSource.QueueName);

                var listener = _amqpService.GetAmqpListener(OnMessageReceived, dataSource.QueueName);
                _listeners.Add(dataSource.NameId, listener);
                //GetConnection(dataSource.QueueName);
//                con.MessageReceiver = OnMessageReceived;
//                con.StartListening();

            }

            _dataSources.Add(dataSource.NameId, dataSource);
        }

        public void RemoveDataSource(ValueDataSource dataSource)
        {
            _dataSources.Remove(dataSource.NameId);

            // ReSharper disable once InvertIf
            if (!_dataSources.Values.Any(x => x.QueueName.Equals(dataSource.QueueName)))
            {
                //No Values more on this Queue
                //=> Stop Listening on this queue
                _listeners[dataSource.NameId].Disconnect();
//                _amqpService.GetConnection(dataSource.QueueName).StopListening();
                _logger.LogInformation("Stop listening for ValueData on Queue \"{0}\"", dataSource.QueueName);
            }
        }

        private bool OnMessageReceived(Message message)
        {
            var parser = new ValueMessageParser();

            if (!parser.IsResponsible(message))
            {
                return false;
            }

            try
            {
                var value = parser.ParseMessage(message);
                SetValue(value.ConvertToValue());
            }
            catch (MessageParsingException e)
            {
                _logger.LogError("Exception while parsing the Message:", e);
            }

            return true;
        }

        public void UpdateDataSources(List<ValueDataSource> sources)
        {
            var comp = new ValueDataSourcComparer();

            //Determine old
            // ReSharper disable once ImplicitlyCapturedClosure
            var olds = _dataSources.Values.Where(x => !sources.Contains(x, comp));
            foreach (var ds in olds)
            {
                RemoveDataSource(ds);
            }

            //Determine new
            // ReSharper disable once ImplicitlyCapturedClosure
            var news = sources.Where(x => !_dataSources.Values.Contains(x, comp));
            foreach (var ds in news)
            {
                AddDataSource(ds);
            }
        }

        private class ValueDataSourcComparer : IEqualityComparer<ValueDataSource>
        {
            public bool Equals(ValueDataSource x, ValueDataSource y)
            {
                return x.CompareTo(y) == 0;
            }

            public int GetHashCode(ValueDataSource obj)
            {
                return (obj.QueueName + ":" + obj.NameId).GetHashCode();
            }
        }
    }
}