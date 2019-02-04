//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using SmartDevicesGateway.Common.Extensions;
using SmartDevicesGateway.Model.Extensions;
using SmartDevicesGateway.Model.Jobs;

namespace SmartDevicesGateway.Processing.Controller.Util
{
    public static class JobFilter
    {
        public static void AddStatusFilter(ref IQueryable<Job> query, string filterStateBy, bool filterStateExcluding)
        {
            if (!string.IsNullOrEmpty(filterStateBy))
            {
                query = query.WhereContains("Status", filterStateBy, filterStateExcluding);
            }
        }

        public static void AddFilter(ref IQueryable<Job> query, string filterBy, string filterString, bool filterExcluding)
        {
            if (!string.IsNullOrEmpty(filterBy) && !string.IsNullOrEmpty(filterString))
            {
                var capitalBy = filterBy.ToFirstCharUpperCase();

                if (typeof(Job).GetProperty(capitalBy) != null)
                {
                    //Property exists
                    query = query.WhereContains(filterBy, filterString, filterExcluding);
                }
                else
                {
                    //Filter for Resource
                    query = query.Where(x => FilterJob(x, filterBy, filterString, filterExcluding));
                    //                    query = query.WhereContains("Resource."+filterBy, filterString, filterExcluding);
                }
            }
        }

        public static bool FilterJob(Job x, string filterBy, string filterString, bool filterExcluding)
        {
            var b = false;
            if (filterBy.Equals("text"))
            {
                if (x.Resource.ContainsKey("text") && x.Resource["text"].Contains(filterString))
                {
                    b = true;
                }
                if (x.Resource.ContainsKey("list_text") && x.Resource["list_text"].Contains(filterString))
                {
                    b = true;
                }
                if (x.Resource.ContainsKey("list_title") && x.Resource["list_title"].Contains(filterString))
                {
                    b = true;
                }
            }
            else if (x.Resource.ContainsKey(filterBy))
            {
                b = x.Resource[filterBy].Equals(filterString);
            }
            return filterExcluding ? !b : b;
        }

        public static void AddSort(ref IQueryable<Job> query, string sortBy, bool sortAscending)
        {
            if (!string.IsNullOrEmpty(sortBy))
            {
                var capitalBy = sortBy.ToFirstCharUpperCase();
                if (typeof(Job).GetProperty(capitalBy) != null)
                {
                    query = sortAscending ? query.OrderBy(sortBy) : query.OrderByDescending(sortBy);
                }
                else
                {
                    query = sortAscending ? query.OrderBy(x => JobSortSelector(x, sortBy)) :
                        query.OrderByDescending(x => JobSortSelector(x, sortBy));
                }
            }
        }

        public static void AddJobSort(ref IQueryable<Job> query, string sortBy, bool sortAscending)
        {
            if (string.IsNullOrEmpty(sortBy) || 
                string.Equals(sortBy, "createdate", StringComparison.CurrentCultureIgnoreCase) ||
                string.Equals(sortBy, "date", StringComparison.CurrentCultureIgnoreCase))
            {
                //TODO Read default order
                sortBy = "createdAt";
            }

            AddSort(ref query, sortBy, sortAscending);
        }

        public static object JobSortSelector(Job x, string sortBy)
        {
            if (x.Resource.ContainsKey(sortBy))
            {
                return x.Resource[sortBy];
            }
            return null;
        }

    }
}
