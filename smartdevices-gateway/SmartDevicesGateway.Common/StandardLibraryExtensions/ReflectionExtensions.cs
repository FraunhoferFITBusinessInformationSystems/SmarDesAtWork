//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Text;

namespace Common.StandardLibraryExtensions
{
    public static class ReflectionExtensions
    {
        public static TProperty GetProperty<TProperty>(this object entity, string propertyName)
        {
            var propInfo = entity.GetType().GetProperty(propertyName);
            return (TProperty)propInfo.GetValue(entity);
        }

        public static void SetProperty<TProperty>(this object entity, string propertyName, TProperty value)
        {
            var propInfo = entity.GetType().GetProperty(propertyName);
            propInfo.SetValue(entity, value);
        }

        public static TProperty GetPropertyRecursive<T, TProperty>(this T entity, string propertyName)
        {
            object obj = entity;
            var i = propertyName.IndexOf(".", StringComparison.Ordinal);
            while (i >= 0)
            {
                var subProperty = propertyName.Substring(0, i);
                propertyName = propertyName.Substring(i + 1);

                var propInfo = entity.GetType().GetProperty(subProperty);
                obj = propInfo.GetValue(obj);

                i = propertyName.IndexOf(".", StringComparison.Ordinal);
            }
            return (TProperty)obj;
        }

        public static void SetPropertyRecursive<T, TProperty>(this T entity, string propertyName,
            TProperty value)
        {
            object obj = entity, subObj = entity;
            PropertyInfo propInfo = null;
            var i = propertyName.IndexOf(".", StringComparison.Ordinal);
            while (i >= 0)
            {
                obj = subObj;
                var subProperty = propertyName.Substring(0, i);
                propertyName = propertyName.Substring(i + 1);

                propInfo = entity.GetType().GetProperty(subProperty);
                subObj = propInfo.GetValue(obj);

                i = propertyName.IndexOf(".", StringComparison.Ordinal);
            }
            propInfo?.SetValue(obj, value);
        }
    }
}
