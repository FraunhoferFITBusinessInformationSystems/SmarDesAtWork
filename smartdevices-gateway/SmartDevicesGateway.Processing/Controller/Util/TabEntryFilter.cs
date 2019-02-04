using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using System.Text;
using Common.StandardLibraryExtensions;
using SmartDevicesGateway.Common.Extensions;
using SmartDevicesGateway.Model.Dto.Data;
using SmartDevicesGateway.Model.Extensions;
using SmartDevicesGateway.Model.Jobs;
using SmartDevicesGateway.Model.Ui;

namespace SmartDevicesGateway.Processing.Controller.Util
{
    [SuppressMessage("ReSharper", "EnforceIfStatementBraces")]
    public static class TabEntryFilter
    {
        public static void AddFilter(ref IQueryable<TabEntryDto> query, string filterBy, string filterString, bool filterExcluding)
        {
            if (string.IsNullOrEmpty(filterBy) || string.IsNullOrEmpty(filterString))
            {
                return;
            }

            var selector = TabEntryPropertySelector(filterBy);
            var filter = new Func<TabEntryDto, bool>(x =>
            {
                var b = filterString.Contains(selector(x).ToString(), StringComparison.CurrentCultureIgnoreCase);
                return filterExcluding ? !b : b;
            });

            query = query.Where(x => filter(x));
        }
        
        public static void AddSort(ref IQueryable<TabEntryDto> query, string sortBy, bool sortAscending)
        {
            if (string.IsNullOrEmpty(sortBy) ||
                string.Equals(sortBy, "createdate", StringComparison.CurrentCultureIgnoreCase) ||
                string.Equals(sortBy, "date", StringComparison.CurrentCultureIgnoreCase))
            {
                //TODO Read default order
                sortBy = "createdAt";
            }

            if (string.IsNullOrEmpty(sortBy))
                return;

            var selector = TabEntryPropertySelector(sortBy);

            query = sortAscending ? query.OrderBy(x => selector(x)) :
                query.OrderByDescending(x => selector(x));
        }
        
        private static Func<TabEntryDto, object> TabEntryPropertySelector(string propertyName)
        {
            if (string.IsNullOrEmpty(propertyName))
            {
                //TODO use default order from TabConfig
                propertyName = "createdAt";
            }

            var capitalBy = propertyName.ToFirstCharUpperCase();
            
            //Predefined selectors on some properties:
            if (capitalBy.Equals("Status"))
            {
                return x => x.Entry?.Status;
            }
            if (capitalBy.Equals("Createdate") || capitalBy.Equals("date"))
            {
                return x => x.Entry?.CreatedAt;
            }
            if (capitalBy.Equals("Text"))
            {
                return x => (x.Entry?.Resource == null) ? null :
                    x.Entry.Resource.ContainsKey("text") ? x.Entry.Resource["text"] :
                    x.Entry.Resource.ContainsKey("list_text") ? x.Entry.Resource["list_text"] : null;
            }
            if (capitalBy.Equals("Title"))
            {
                return x => (x.Entry?.Resource == null) ? null :
                    x.Entry.Resource.ContainsKey("title") ? x.Entry.Resource["title"] :
                    x.Entry.Resource.ContainsKey("list_title") ? x.Entry.Resource["list_title"] : null;
            }

            //Return from UiComponent if Property in UiComponent exists
            if (typeof(UiComponent).GetProperty(capitalBy) != null)
                return x => x.ListUi?.GetProperty<object>(capitalBy);

            //Return from Job-Object if Property exists
            if (typeof(Job).GetProperty(capitalBy) != null)
                return x => x.Entry?.GetProperty<object>(capitalBy);
            
            //Return from Job-Resources if Key exists
            return x => (x.Entry?.Resource != null) 
                ? (x.Entry.Resource.ContainsKey(propertyName) ? x.Entry.Resource[propertyName] : null)
                : null;
        }
    }
}
