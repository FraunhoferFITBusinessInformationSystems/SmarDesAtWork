//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;

namespace Common.Caching
{
    /// <summary>
    /// The non-generic Cache class instanciates a Cache{object} that can be used with any type of (mixed) contents.
    /// It also publishes a static member, so a cache can be used even without creating a dedicated instance.
    /// The <c>.Global</c> member is lazy instanciated.
    /// </summary>
    public class GlobalCache : GenericKeyValueCache<string, object>
    {
        /// <summary>
        /// The lazy holder
        /// </summary>
        private static readonly Lazy<GlobalCache> LazyHolder
            = new Lazy<GlobalCache>(() => new GlobalCache());


        /// <summary>
        /// Prevents a default instance of the <see cref="GlobalCache"/> class from being created.
        /// </summary>
        private GlobalCache()
        { 
        }


        /// <summary>
        /// Gets the global shared cache instance valid for the entire process.
        /// </summary>
        /// <value>
        /// The global shared cache instance.
        /// </value>
        public static GlobalCache Instance => LazyHolder.Value;
    }
}