//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.ComponentModel.DataAnnotations;
using SmartDevicesGateway.Model.Requests;

namespace SmartDevicesGateway.Api.Requests
{
    public class SortRequestDto
    {
        public string SortBy { get; set; }

        public bool SortOrderAscending { get; set; }

        public static SortRequest MapSortRequest(SortRequestDto dto)
        {
            return new SortRequest
            {
                SortBy = dto.SortBy,
                SortOrderAscending = dto.SortOrderAscending
            };
        }
    }
}
