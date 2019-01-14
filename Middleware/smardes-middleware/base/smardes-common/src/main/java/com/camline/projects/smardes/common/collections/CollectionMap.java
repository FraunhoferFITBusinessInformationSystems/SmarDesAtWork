/*******************************************************************************
 * Copyright (C) 2018-2019 camLine GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
/*
 * Created on Mar 3, 2005
 */
package com.camline.projects.smardes.common.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Implementation of a map of collections (Set or List depending on Order).
 *
 * For the outer Map we support the following orders:
 * <ul>
 * <li>SORTED (backed by TreeMap)</li>
 * <li>APPEARANCE (backed by LinkedHashMap)</li>
 * <li>NONE (backed by HashMap)</li>
 * </ul>
 * For the inner Collection (Map value) we support the following orders
 * <ul>
 * <li>SORTED (backed by TreeSet)</li>
 * <li>APPEARANCE (backed by LinkedHashSet)</li>
 * <li>LIST (backed by ArrayList)</li>
 * <li>NONE (backed by HashSet)</li>
 * </ul>
 *
 * @author matze
 *
 * @param <K> key type of the map
 * @param <V> value type of the set, which is the map value
 */
public class CollectionMap<K,V> {
	/**
	 * Enum for different orders of key set and collection values
	 *
	 * @author matze
	 *
	 */
	public enum Order {
		/** No order at all (map: HashMap, value: HashSet */
		NONE,
		/** Sorted order (map: TreeMap, value: TreeSet */
		SORTED,
		/** Order of appearance (map: LinkedHashMap, value: LinkedHashSet */
		APPEARANCE,
		/** Use a List for values (map: unsupported, value: ArrayList */
		LIST
	}

	private final Order orderInner;
	private final Comparator<? super V> innerComparator;
	private final Map<K,Collection<V>> collectionMap;

	/**
	 * Default constructor.
	 */
	public CollectionMap() {
		this(Order.NONE, Order.NONE);
	}

	/**
	 * Constructor where it is possible to define the order of keys and values
	 * @param keyOrder entry order based on map key set
	 * @param valueOrder value order in each collection
	 */
	public CollectionMap(final Order keyOrder, final Order valueOrder) {
		this(keyOrder, null, valueOrder, null);
	}

	/**
	 * Constructor where it is possible to define the order of keys and values
	 * @param keyOrder entry order based on map key set
	 * @param keyComparator comparator for entry order (only for SORTED)
	 * @param valueOrder value order in each collection
	 * @param valueComparator comparator for value order (only for SORTED)
	 */
	public CollectionMap(final Order keyOrder, final Comparator<? super K> keyComparator,
			final Order valueOrder, final Comparator<? super V> valueComparator) {
		this.orderInner = valueOrder;
		this.innerComparator = valueComparator;
		this.collectionMap = createOuterMap(keyOrder, keyComparator);
	}


	private Map<K,Collection<V>> createOuterMap(final Order orderOuter, final Comparator<? super K> outerComparator) {
		switch (orderOuter) {
		case SORTED:
			return new TreeMap<>(outerComparator);
		case APPEARANCE:
			return new LinkedHashMap<>();
		case LIST:
			throw new IllegalArgumentException("List ORDER makes no sense for the Map");
		case NONE:
		default:
			return new HashMap<>();
		}
	}

	/**
	 * Adds an element to the set for the given key.
	 * If the set does not exist for this key, it gets
	 * created.
	 * @param key key where to find the set where this element should be added
	 * @param value element to be added
	 * @return the return value of the Set.add() method
	 * @see Set#add(Object)
	 */
	public boolean add(final K key, final V value) {
		final Collection<V> set = getOrCreateValues(key);
		return set.add(value);
	}

	/**
	 * Adds a collection to the set for the given key.
	 * If the set does not exist for this key, it gets
	 * created.
	 * @param key key where to find the set where this element should be added
	 * @param values collection to be added
	 * @return the return value of the Set.add() method
	 * @see Set#addAll(Collection)
	 */
	public boolean addAll(final K key, final Collection<V> values) {
		final Collection<V> set = getOrCreateValues(key);
		return set.addAll(values);
	}

	/**
	 * Adds an iterable to the set for the given key.
	 * If the set does not exist for this key, it gets
	 * created.
	 * @param key key where to find the set where this element should be added
	 * @param values iterable values to be added
	 * @return the return value of the Set.add() method
	 * @see Set#addAll(Collection)
	 */
	public boolean addAll(final K key, final Iterable<V> values) {
		final Collection<V> set = getOrCreateValues(key);
		boolean changed = false;
		for (final V value : values) {
			changed |= set.add(value);
		}
		return changed;
	}

	/**
	 * Add a bunch values to the set for the given key.
	 * If the set does not exist for this key, it gets
	 * created.
	 * @param key key where to find the set where this element should be added
	 * @param values variable list of values
	 * @return the return value of the Set.add() method
	 * @see Set#addAll(Collection)
	 */
	@SafeVarargs
	public final boolean addAll(final K key, final V... values) {
		final Collection<V> set = getOrCreateValues(key);
		boolean changed = false;
		for (final V value : values) {
			changed |= set.add(value);
		}
		return changed;
	}

	/**
	 * @param key key
	 * @return true if the map contains the key already
	 */
	public boolean containsKey(final K key) {
		return collectionMap.containsKey(key);
	}

	/**
	 * Adds a collection to the set for the given key.
	 * If the set does not exist for this key, it gets
	 * created. This methods keeps the order of the key.
	 * @param key key where to find the set where this element should be added
	 * @param values collection to be added
	 * @see Set#addAll(Collection)
	 */
	public void replaceAll(final K key, final Collection<V> values) {
		final Collection<V> set = getOrCreateValues(key);
		set.clear();
		set.addAll(values);
	}

	/**
	 * Adds an iterable to the set for the given key.
	 * If the set does not exist for this key, it gets
	 * created.
	 * @param key key where to find the set where this element should be added
	 * @param values iterable values to be added
	 * @see Set#addAll(Collection)
	 */
	public void replaceAll(final K key, final Iterable<V> values) {
		final Collection<V> set = getOrCreateValues(key);
		set.clear();
		for (final V value : values) {
			set.add(value);
		}
	}

	/**
	 * Add a bunch values to the set for the given key.
	 * If the set does not exist for this key, it gets
	 * created.
	 * @param key key where to find the set where this element should be added
	 * @param values variable list of values
	 * @see Set#addAll(Collection)
	 */
	@SafeVarargs
	public final void replaceAll(final K key, final V... values) {
		final Collection<V> set = getOrCreateValues(key);
		set.clear();
		for (final V value : values) {
			set.add(value);
		}
	}


	/**
	 * Remove the complete entry with the collection.
	 *
	 * @param key key where to find this collection being removed
	 * @return collection
	 */
	public Collection<V> remove(final K key) {
		return collectionMap.remove(key);
	}

	/**
	 * Removes the specified element from the set for the given key.
	 * If set becomes empty, the map entry gets also removed.
	 * @param key key where to find the set where this element should be removed
	 * @param value the object to be removed
	 * @return the return value of the Set.add() method
	 * @see Set#remove(Object)
	 */
	public boolean remove(final K key, final V value) {
		final Collection<V> set = getValues(key);
		if (set == null) {
			return false;
		}

		final boolean retval = set.remove(value);
		if (set.isEmpty()) {
			collectionMap.remove(key);
		}
		return retval;
	}

	/**
	 * Returns the set to which the specified key is mapped,
	 * or null if this map contains no mapping for the key.
	 * @param key map key
	 * @return set or null
	 * @see Map#get(Object)
	 */
	public Collection<V> getValues(final K key) {
		return collectionMap.get(key);
	}

	/**
	 * Convenience variant for getSet(Object) method, which returns
	 * an empty set instead of null.
	 * @param key map key
	 * @return set or empty set
	 * @see #getValues(Object)
	 */
	public Collection<V> safeGetValues(final K key) {
		final Collection<V> set = getValues(key);
		if (set != null) {
			return set;
		}
		return Collections.emptySet();
	}

	/**
	 * Convenience variant for getSet(Object) method, which returns
	 * an empty set instead of null.
	 * @param key map key
	 * @return set or empty set
	 * @see #getValues(Object)
	 */
	public Collection<V> getOrCreateValues(final K key) {
		Collection<V> set = getValues(key);
		if (set == null) {
			set = createInnerCollection();
			collectionMap.put(key, set);
		}
		return set;
	}

	private Collection<V> createInnerCollection() {
		switch (orderInner) {
		case SORTED:
			return new TreeSet<>(innerComparator);
		case APPEARANCE:
			return new LinkedHashSet<>();
		case LIST:
			return new ArrayList<>();
		case NONE:
		default:
			return new HashSet<>();
		}
	}

	/**
	 * Wrapper for map's internal keySet()
	 * @return keySet of the map
	 * @see Map#keySet()
	 */
	public Set<K> keySet() {
		return collectionMap.keySet();
	}

	/**
	 * Wrapper for map's internal entrySet()
	 * @return entrySet of the map
	 * @see Map#entrySet()
	 */
	public Set<Map.Entry<K, Collection<V>>> entrySet() {
		return collectionMap.entrySet();
	}

	/**
	 * Wrapper for map's internal values()
	 * @return value collection of the map
	 * @see Map#entrySet()
	 */
	public Collection<Collection<V>> values() {
		return collectionMap.values();
	}

	/**
	 * Return all values as a flat collection.
	 * Note that depending on the inner collection type, some values might
	 * overwrite others!
	 * @return value collection
	 */
	public Collection<V> flatValues() {
		final Collection<V> flatValues = createInnerCollection();
		for (final Collection<V> values : values()) {
			flatValues.addAll(values);
		}
		return flatValues;
	}

	/**
	 * @return a flat map with values as keys and keys as values
	 */
	public Map<V, K> reverseFlatMap() {
		final Map<V, K> map = new HashMap<>();
		for (final Entry<K, Collection<V>> entry : collectionMap.entrySet()) {
			for (final V value : entry.getValue()) {
				map.put(value, entry.getKey());
			}
		}
		return map;
	}

	/**
	 * Wrapper for map's internal size().
	 * So it is the number of keys, not the total number of elements added to sets.
	 * @return size of the internal map
	 * @see Map#entrySet()
	 */
	public int size() {
		return collectionMap.size();
	}

	/**
	 * Wrapper for map's internal isEmpty().
	 * @return true if map is empty
	 */
	public boolean isEmpty() {
		return collectionMap.isEmpty();
	}

	/**
	 * @param key map key
	 * @return size of a set for a given key
	 */
	public int size(final K key) {
		final Collection<V> set = getValues(key);
		if (set == null) {
			return 0;
		}
		return set.size();
	}

	@Override
	public String toString() {
		return collectionMap.toString();
	}
}
