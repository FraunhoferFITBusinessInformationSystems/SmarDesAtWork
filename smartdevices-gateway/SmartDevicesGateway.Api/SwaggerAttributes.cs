//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Linq;
using Swashbuckle.AspNetCore.Swagger;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace SmartDevicesGateway.Api
{
    public class BinaryBodyPayloadAttribute : Attribute
    {
        public BinaryBodyPayloadAttribute()
        {
            ParameterName = "payload";
            Required = true;
            MediaType = "application/octet-stream";
            Format = "binary";
        }

        public string Format { get; set; }

        public string MediaType { get; set; }

        public bool Required { get; set; }

        public string ParameterName { get; set; }
    }

    public class JsonBodyPayloadAttribute : Attribute
    {
        public JsonBodyPayloadAttribute()
        {
            ParameterName = "payload";
            Required = true;
            MediaType = "application/json";
            Format = "application/json";
        }

        public string Format { get; set; }

        public string MediaType { get; set; }

        public bool Required { get; set; }

        public string ParameterName { get; set; }
    }

    /// <summary>
    /// Filter for a controller action that accept a binary payload.
    /// </summary>
    public class BinaryBodyPayloadFilter : IOperationFilter
    {
        /// <summary>
        /// Applies the specified operation.
        /// </summary>
        /// <param name="operation">The operation.</param>
        /// <param name="context">The context.</param>
        public void Apply(Swashbuckle.AspNetCore.Swagger.Operation operation, OperationFilterContext context)
        {
            context.ApiDescription.TryGetMethodInfo(out var test);
            var attribute = test.GetCustomAttributes(true)
                    .FirstOrDefault(x => x is BinaryBodyPayloadAttribute) as BinaryBodyPayloadAttribute;
            if (attribute == null)
            {
                return;
            }

            operation.Consumes.Clear();
            operation.Consumes.Add(attribute.MediaType);
            operation.Parameters.Add(new BodyParameter
            {
                Name = attribute.ParameterName,
                Required = attribute.Required
            });
        }
    }

    /// <summary>
    /// Filter for a controller action that accept a binary payload.
    /// </summary>
    public class JsonBodyPayloadFilter : IOperationFilter
    {
        /// <summary>
        /// Applies the specified operation.
        /// </summary>
        /// <param name="operation">The operation.</param>
        /// <param name="context">The context.</param>
        public void Apply(Swashbuckle.AspNetCore.Swagger.Operation operation, OperationFilterContext context)
        {
            context.ApiDescription.TryGetMethodInfo(out var test);
            var attribute = test.GetCustomAttributes(true)
                .FirstOrDefault(x => x is JsonBodyPayloadAttribute) as JsonBodyPayloadAttribute;
            if (attribute == null)
            {
                return;
            }

            operation.Consumes.Clear();
            operation.Consumes.Add(attribute.MediaType);
            operation.Parameters.Add(new BodyParameter
            {
                Name = attribute.ParameterName,
                Required = attribute.Required
            });
        }
    }
}
