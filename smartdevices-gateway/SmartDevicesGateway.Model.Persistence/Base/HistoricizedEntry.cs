//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;

namespace SmartDevicesGateway.Model.Persistence.Base
{
    public class HistoricizedEntry<T>
    {
        private readonly LinkedList<ValueSnapshot<T>> _valueHistory = new LinkedList<ValueSnapshot<T>>();

        public int MaxHistoryEntries { get; set; } = 1;
        public TimeSpan MaxHistoryLivetime { get; set; } = TimeSpan.Zero;

        public IEnumerable<ValueSnapshot<T>> ValueHistory => _valueHistory;

        private ValueSnapshot<T> First => _valueHistory.First?.Value;

        public T Value
        {
            get => _valueHistory.First?.Value == null ? default(T) : _valueHistory.First.Value.Value;
            set
            {
                _valueHistory.AddFirst(new ValueSnapshot<T> { Date = DateTimeOffset.Now, Value = value });
                CleanupHistory();
            }
        }
        public void CleanupHistory()
        {
            //check length
            if (MaxHistoryEntries >= 0)
            {
                var tooMuch = _valueHistory.Count - MaxHistoryEntries;
                while (tooMuch > 0)
                {
                    tooMuch--;
                    _valueHistory.RemoveLast();
                }
            }
            //check livetime
            // ReSharper disable once InvertIf
            if (MaxHistoryLivetime != TimeSpan.Zero)
            {
                var now = DateTimeOffset.Now;
                while ((now - _valueHistory.Last.Value.Date) >= MaxHistoryLivetime)
                {
                    _valueHistory.RemoveLast();
                }
            }
        }
        public int Count => _valueHistory.Count;
    }
}