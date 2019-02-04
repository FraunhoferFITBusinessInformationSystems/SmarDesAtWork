//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using SmartDevicesGateway.Model.Requests;

namespace SmartDevicesGateway.Api.Requests
{
    public class AdvancedFilterRequestDto
    {
        public bool[] FilterExcluding { get; set; }

        public string[] FilterBy { get; set; }

        public string[] FilterString { get; set; }

        public static IList<AdvancedFilterRequest> ParseFilterRequest(AdvancedFilterRequestDto filterRequest)
        {
            var filters = new List<AdvancedFilterRequest>();
            if (filterRequest.FilterBy == null)
            {
                return filters;
            }

            filters.AddRange(filterRequest.FilterBy.Select((t, index) => new AdvancedFilterRequest()
            {
                FilterBy = t,
                FilterString = filterRequest.FilterString[index],
                FilterExcluding = filterRequest.FilterExcluding[index],
            }));

            return filters;
        }
    }
}
