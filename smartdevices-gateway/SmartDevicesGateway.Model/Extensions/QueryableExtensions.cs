//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Linq.Expressions;
using System.Linq;
using System.Threading.Tasks;



// ReSharper disable once CheckNamespace
namespace SmartDevicesGateway.Model.Extensions
{
    public static class QueryableExtensions
    {
        public static PagedList<T> ToPagedList<T>(this IQueryable<T> queryable, int pageNumber,
            int pageSize)
        {
            return PagedList<T>.Create(queryable, pageNumber, pageSize);
        }

        public static IOrderedQueryable<T> OrderBy<T>(this IQueryable<T> source, string property)
        {
            return ApplyOrder(source, property, "OrderBy");
        }

        public static IOrderedQueryable<T> OrderByDescending<T>(this IQueryable<T> source, string property)
        {
            return ApplyOrder(source, property, "OrderByDescending");
        }

        public static IOrderedQueryable<T> ThenBy<T>(this IOrderedQueryable<T> source, string property)
        {
            return ApplyOrder(source, property, "ThenBy");
        }

        public static IOrderedQueryable<T> ThenByDescending<T>(this IOrderedQueryable<T> source, string property)
        {
            return ApplyOrder(source, property, "ThenByDescending");
        }

        private static IOrderedQueryable<T> ApplyOrder<T>(IQueryable<T> source, string property, string methodName)
        {
            var props = property.Split('.');
            var type = typeof(T);
            var arg = Expression.Parameter(type, "x");

            Expression expr = arg;

            foreach (var prop in props)
            {
                // use reflection (not ComponentModel) to mirror LINQ
                var propertyInfo = type.GetProperty(prop.FirstLetterToUpper());
                expr = Expression.Property(expr, propertyInfo);
                type = propertyInfo.PropertyType;
            }

            var delegateType = typeof(Func<,>).MakeGenericType(typeof(T), type);
            var lambda = Expression.Lambda(delegateType, expr, arg);

            var result = typeof(Queryable).GetMethods().Single(
                    method => method.Name == methodName
                              && method.IsGenericMethodDefinition
                              && method.GetGenericArguments().Length == 2
                              && method.GetParameters().Length == 2)
                .MakeGenericMethod(typeof(T), type)
                .Invoke(null, new object[] {source, lambda});

            return (IOrderedQueryable<T>) result;
        }

        public static IQueryable<T> WhereContains<T>(this IQueryable<T> queryable, string filterBy, string filterString, bool filterExcluding)
        {
            var props = filterBy.Split('.');
            var type = typeof(T);
            var parameterExpression = Expression.Parameter(typeof(T), "x");
            Expression expr = parameterExpression;

            foreach (var prop in props)
            {
                var propertyInfo = type.GetProperty(prop.FirstLetterToUpper());
                expr = Expression.Property(expr, propertyInfo);
                type = propertyInfo.PropertyType;
            }

            if (expr.Type != typeof(string))
            {
                // TODO .ToString() sorgt für Laden aller Daten in den Speicher. Ohne wird die komplette Expression in SQL übersetzt.
                expr = Expression.Call(expr, expr.Type.GetMethod("ToString", new Type[] { }));
            }

            var toLowerMethod = expr.Type.GetMethod("ToLower", new Type[] { });

            if (toLowerMethod != null)
            {
                expr = Expression.Call(expr, toLowerMethod);
            }

            var containsMethod = typeof(string).GetMethod("Contains", new[] {typeof(string)});
            var containsExpr = Expression.Call(expr, containsMethod, Expression.Constant(filterString.ToLower()));

            var lambda = Expression.Lambda(containsExpr, parameterExpression);
            if (filterExcluding)
            {
                lambda = Expression.Lambda(Expression.Not(containsExpr), parameterExpression);
            }

            var whereCallExpression = Expression.Call(
                typeof(Queryable),
                "Where",
                new[] {typeof(T)},
                queryable.Expression,
                lambda);

            return queryable.Provider.CreateQuery<T>(whereCallExpression);
        }

        public static IQueryable<T> WhereEquals<T>(this IQueryable<T> queryable, string filterBy, string filterString)
        {
            var props = filterBy.Split('.');
            var type = typeof(T);
            var parameterExpression = Expression.Parameter(typeof(T), "x");
            Expression expr = parameterExpression;

            foreach (var prop in props)
            {
                var propertyInfo = type.GetProperty(prop.FirstLetterToUpper());
                expr = Expression.Property(expr, propertyInfo);
                type = propertyInfo.PropertyType;
            }

            if (expr.Type != typeof(string))
            {
                // TODO .ToString() sorgt für Laden aller Daten in den Speicher. Ohne wird die komplette Expression in SQL übersetzt.
                expr = Expression.Call(expr, expr.Type.GetMethod("ToString", new Type[] { }));
            }

            var toLowerMethod = expr.Type.GetMethod("ToLower", new Type[] { });

            if (toLowerMethod != null)
            {
                expr = Expression.Call(expr, toLowerMethod);
            }

            var equalExpr = Expression.Equal(expr, Expression.Constant(filterString.ToLower()));

            var whereCallExpression = Expression.Call(
                typeof(Queryable),
                "Where",
                new[] {typeof(T)},
                queryable.Expression,
                Expression.Lambda(equalExpr, parameterExpression));

            return queryable.Provider.CreateQuery<T>(whereCallExpression);
        }
    }
}