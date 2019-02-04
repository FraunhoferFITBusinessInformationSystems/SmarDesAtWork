//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Diagnostics.CodeAnalysis;

namespace SmartDevicesGateway.Model.Ui
{
    public class UiAction : UiComponent
    {
        /// <summary>
        /// Target job of this Action
        /// </summary>
        public string JobKey { get; set; }

        public UiAction()
        {
            Tab = "actions";
        }

        [SuppressMessage("ReSharper", "InvertIf")]
        public UiAction(UiComponent other) : base(other)
        {
            const string key = "JobKey";
            if (AdditionalProperties.ContainsKey(key))
            {
                JobKey = AdditionalProperties[key]?.ToString();
                AdditionalProperties.Remove(key);
            }
        }
    }
}
