//
// Copyright (c) Vogler Engineering GmbH. All rights reserved.
// Licensed under the MIT License. See LICENSE.md in the project root for license information.
//
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;

namespace Common.Caching
{
    /// <summary>
    /// This is a generic cache subsystem based on key/value pairs, where key is generic, too. Key must be unique.
    /// Every cache entry has its own timeout.
    /// Cache is thread safe and will delete expired entries on its own using System.Threading.Timers (which run on ThreadPool threads).
    /// </summary>
    public class GenericKeyValueCache<TK, T> : IDisposable
    {
        private readonly Dictionary<TK, T> _cache = new Dictionary<TK, T>();
        private readonly Dictionary<TK, Timer> _timers = new Dictionary<TK, Timer>();
        private readonly ReaderWriterLockSlim _locker = new ReaderWriterLockSlim();
        private bool _disposed;


        /// <summary>
        /// Adds or updates the specified cache-key with the specified cacheObject and applies a specified timeout (in seconds) to this key.
        /// </summary>
        /// <param name="key">The cache-key to add or update.</param>
        /// <param name="cacheObject">The cache object to store.</param>
        /// <param name="cacheTimeout">The cache timeout (lifespan) of this object. Must be 1 or greater.
        /// Specify Timeout.Infinite to keep the entry forever.</param>
        /// <param name="restartTimerIfExists">(Optional). If set to <c>true</c>, the timer for this cacheObject will be reset if the object already
        /// exists in the cache. (Default = false).</param>
        public void AddOrUpdate(TK key, T cacheObject, int cacheTimeout, bool restartTimerIfExists = false)
        {
            if (_disposed) return;

            if (cacheTimeout != Timeout.Infinite && cacheTimeout < 1)
            {
                throw new ArgumentOutOfRangeException(nameof(cacheTimeout));
            }

            _locker.EnterWriteLock();
            try
            {
                CheckTimer(key, cacheTimeout, restartTimerIfExists);

                if (!_cache.ContainsKey(key))
                    _cache.Add(key, cacheObject);
                else
                    _cache[key] = cacheObject;
            }
            finally { _locker.ExitWriteLock(); }
        }


        /// <summary>
        /// Adds or updates the specified cache-key with the specified cacheObject and applies <c>Timeout.Infinite</c> to this key.
        /// </summary>
        /// <param name="key">The cache-key to add or update.</param>
        /// <param name="cacheObject">The cache object to store.</param>
        public void AddOrUpdate(TK key, T cacheObject)
        {
            AddOrUpdate(key, cacheObject, Timeout.Infinite);
        }


        /// <summary>
        /// Gets the cache entry with the specified key or returns <c>default(T)</c> if the key is not found.
        /// </summary>
        /// <param name="key">The cache-key to retrieve.</param>
        /// <returns>The object from the cache or <c>default(T)</c>, if not found.</returns>
        public T this[TK key] => Get(key);


        /// <summary>
        /// Gets the cache entry with the specified key or return <c>default(T)</c> if the key is not found.
        /// </summary>
        /// <param name="key">The cache-key to retrieve.</param>
        /// <returns>The object from the cache or <c>default(T)</c>, if not found.</returns>
        public T Get(TK key)
        {
            if (_disposed) return default(T);

            _locker.EnterReadLock();
            try
            {
                T rv;
                return (_cache.TryGetValue(key, out rv) ? rv : default(T));
            }
            finally { _locker.ExitReadLock(); }
        }


        /// <summary>
        /// Tries to gets the cache entry with the specified key.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="value">(out) The value, if found, or <c>default(T)</c>, if not.</param>
        /// <returns><c>True</c>, if <c>key</c> exists, otherwise <c>false</c>.</returns>
        public bool TryGet(TK key, out T value)
        {
            if (_disposed)
            {
                value = default(T);
                return false;
            }

            _locker.EnterReadLock();
            try
            {
                return _cache.TryGetValue(key, out value);
            }
            finally { _locker.ExitReadLock(); }
        }


        /// <summary>
        /// Removes a series of cache entries in a single call for all key that match the specified key pattern.
        /// </summary>
        /// <param name="keyPattern">The key pattern to remove. The Predicate has to return true to get key removed.</param>
        public void Remove(Predicate<TK> keyPattern)
        {
            if (_disposed) return;

            _locker.EnterWriteLock();
            try
            {
                var removers = (from k in _cache.Keys
                                where keyPattern(k)
                                select k).ToList();

                foreach (var workKey in removers)
                {
                    try { _timers[workKey].Dispose(); }
                    catch
                    {
                        // ignored
                    }
                    _timers.Remove(workKey);
                    _cache.Remove(workKey);
                }
            }
            finally { _locker.ExitWriteLock(); }
        }


        /// <summary>
        /// Removes the specified cache entry with the specified key.
        /// If the key is not found, no exception is thrown, the statement is just ignored.
        /// </summary>
        /// <param name="key">The cache-key to remove.</param>
        public void Remove(TK key)
        {
            if (_disposed) return;

            _locker.EnterWriteLock();
            try
            {
                if (!_cache.ContainsKey(key))
                {
                    return;
                }

                try { _timers[key].Dispose(); }
                catch
                {
                    // ignored
                }
                _timers.Remove(key);
                _cache.Remove(key);
            }
            finally { _locker.ExitWriteLock(); }
        }


        /// <summary>
        /// Clears the entire cache and disposes all active timers.
        /// </summary>
        public void Clear()
        {
            _locker.EnterWriteLock();
            try
            {
                try
                {
                    foreach (var t in _timers.Values)
                    {
                        t.Dispose();
                    }
                }
                catch
                {
                    // ignored
                }
                _timers.Clear();
                _cache.Clear();
            }
            finally { _locker.ExitWriteLock(); }
        }


        /// <summary>
        /// Checks if a specified key exists in the cache.
        /// </summary>
        /// <param name="key">The cache-key to check.</param>
        /// <returns><c>True</c> if the key exists in the cache, otherwise <c>False</c>.</returns>
        public bool Exists(TK key)
        {
            if (_disposed) return false;

            _locker.EnterReadLock();
            try
            {
                return _cache.ContainsKey(key);
            }
            finally { _locker.ExitReadLock(); }
        }


        /// <summary>
        /// Performs application-defined tasks associated with freeing, releasing, or resetting unmanaged resources.
        /// </summary>
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }


        /// Checks whether a specific timer already exists and adds a new one, if not 
        private void CheckTimer(TK key, int cacheTimeout, bool restartTimerIfExists)
        {
            Timer timer;

            if (_timers.TryGetValue(key, out timer))
            {
                if (restartTimerIfExists)
                {
                    timer.Change(
                        (cacheTimeout == Timeout.Infinite ? Timeout.Infinite : cacheTimeout * 1000),
                        Timeout.Infinite);
                }
            }
            else
                _timers.Add(
                    key,
                    new Timer(
                        RemoveByTimer,
                        key,
                        (cacheTimeout == Timeout.Infinite ? Timeout.Infinite : cacheTimeout * 1000),
                        Timeout.Infinite));
        }


        private void RemoveByTimer(object state)
        {
            Remove((TK)state);
        }

        /// <summary>
        /// Releases unmanaged and - optionally - managed resources.
        /// </summary>
        /// <param name="disposing">
        ///   <c>true</c> to release both managed and unmanaged resources; <c>false</c> to release only unmanaged resources.</param>
        protected virtual void Dispose(bool disposing)
        {
            if (_disposed)
            {
                return;
            }

            _disposed = true;

            if (!disposing)
            {
                return;
            }

            Clear();
            _locker.Dispose();
        }
    }
}