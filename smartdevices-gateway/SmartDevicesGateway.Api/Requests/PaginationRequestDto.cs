//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Mvc;
using SmartDevicesGateway.Model.Requests;

namespace SmartDevicesGateway.Api.Requests
{
    public class PaginationRequestDto
    {
        private const int MaxPageSize = 10000;
        private int _pageSize = 10;


        /// <summary>
        /// Initializes a new instance of the <see cref="PaginationRequestDto"/> class.
        /// </summary>
        public PaginationRequestDto()
        {
            PageNumber = 1;
            PageSize = 100;
        }


        /// <summary>
        /// Gets or sets the page number.
        /// </summary>
        /// <value>
        /// The page number.
        /// </value>
        [Range(0, int.MaxValue)]
        public int PageNumber { get; set; }


        /// <summary>
        /// Gets or sets the size of the page.
        /// </summary>
        /// <value>
        /// The size of the page.
        /// </value>
        [Range(0, MaxPageSize)]
        public int PageSize
        {
            get => _pageSize;
            set => _pageSize = value > MaxPageSize ? MaxPageSize : value;
        }

        public static PaginationRequest MapPaginationRequest(PaginationRequestDto dto)
        {
            return new PaginationRequest
            {
                PageNumber = dto.PageNumber,
                PageSize = dto.PageSize
            };
        }
    }
}
