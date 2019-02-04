//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using Newtonsoft.Json;

namespace SmartDevicesGateway.Model.Values
{
    public class ValueDataSource : IComparable<ValueDataSource>
    {
        public string QueueName { get; set; }
        public string Name { get; set; }
        public string Namespace { get; set; }

        [JsonIgnore]
        public string NameId
        {
            get => Namespace + NameIdSeparator + Name;
            set
            {
                var idx = value.IndexOf(NameIdSeparator, StringComparison.Ordinal);
                if (idx <= 0 || idx == value.Length-1)
                {
                    throw new ArgumentException("No valid NameId for this ValueDataSource");
                }

                Namespace = value.Substring(0, idx);
                Name = value.Substring(idx + 1);
            }
        }

        [JsonIgnore]
        public const string NameIdSeparator = ":";

        public int CompareTo(ValueDataSource other)
        {
            var i = string.Compare(QueueName, other.QueueName, StringComparison.Ordinal);
            if (i != 0)
            {
                return i;
            }

            i = string.Compare(Namespace, other.Namespace, StringComparison.Ordinal);
            return i != 0 ? i : string.Compare(Name, other.Name, StringComparison.Ordinal);
        }
    }
}
