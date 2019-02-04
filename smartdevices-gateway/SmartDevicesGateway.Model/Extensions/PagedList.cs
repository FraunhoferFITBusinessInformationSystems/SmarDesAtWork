//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;


namespace SmartDevicesGateway.Model.Extensions
{
    public class PagedList<T> : List<T>
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="PagedList{T}"/> class.
        /// </summary>
        /// <param name="items">The items.</param>
        /// <param name="count">The count.</param>
        /// <param name="pageNumber">The page number.</param>
        /// <param name="pageSize">Size of the page.</param>
        public PagedList(IEnumerable<T> items, int count, int pageNumber, int pageSize)
        {
            TotalCount = count;
            PageSize = pageSize;
            CurrentPage = pageNumber;
            TotalPages = (int)Math.Ceiling(count / (double)pageSize);
            AddRange(items);
        }


        /// <summary>
        /// Gets the current page.
        /// </summary>
        /// <value>
        /// The current page.
        /// </value>
        public int CurrentPage { get; }


        /// <summary>
        /// Gets the total pages.
        /// </summary>
        /// <value>
        /// The total pages.
        /// </value>
        public int TotalPages { get; }


        /// <summary>
        /// Gets the size of the page.
        /// </summary>
        /// <value>
        /// The size of the page.
        /// </value>
        public int PageSize { get; }


        /// <summary>
        /// Gets the total count.
        /// </summary>
        /// <value>
        /// The total count.
        /// </value>
        public int TotalCount { get; }


        /// <summary>
        /// Gets a value indicating whether the paged list has at least a previous page.
        /// </summary>
        /// <value>
        ///   <c>true</c> if the paged listhas has at least a previous page; otherwise, <c>false</c>.
        /// </value>
        public bool HasPrevious => CurrentPage > 1;


        /// <summary>
        /// Gets a value indicating whether the paged list has at least one next page.
        /// </summary>
        /// <value>
        ///   <c>true</c> if the paged list has at least one next page; otherwise, <c>false</c>.
        /// </value>
        public bool HasNext => CurrentPage < TotalPages;


        /// <summary>
        /// Creates the specified source.
        /// </summary>
        /// <param name="source">The source.</param>
        /// <param name="pageNumber">The page number.</param>
        /// <param name="pageSize">Size of the page.</param>
        /// <returns></returns>
        public static PagedList<T> Create(IQueryable<T> source, int pageNumber, int pageSize)
        {
            var count = source.Count();
            var items = source.Skip((pageNumber - 1) * pageSize).Take(pageSize).ToArray();
            return new PagedList<T>(items, count, pageNumber, pageSize);
        }
    }
}
