/*
 *  Copyright 2016
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.collections;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Naive (not optimized) implementation of an Immutable Map
 *
 * @author F43nd1r
 * @since 4.9.0
 */
public final class ImmutableMap<K, V> implements Map<K, V>, Serializable {

    private final Map<K, V> mMap;

    public ImmutableMap(@NonNull Map<K, V> map) {
        this.mMap = new HashMap<>(map);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Object key) {
        return mMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mMap.containsValue(value);
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> original = mMap.entrySet();
        final ImmutableSet.Builder<Entry<K, V>> builder = new ImmutableSet.Builder<>();
        for (Entry<K, V> entry : original) {
            builder.add(new ImmutableEntryWrapper<>(entry));
        }
        return builder.build();
    }

    @Override
    public V get(Object key) {
        return mMap.get(key);
    }

    @Override
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        return new ImmutableSet<>(mMap.keySet());
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return mMap.size();
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return new ImmutableList<>(mMap.values());
    }

    private static class ImmutableEntryWrapper<K, V> implements Map.Entry<K, V> {
        private final Map.Entry<K, V> mEntry;

        ImmutableEntryWrapper(Entry<K, V> mEntry) {
            this.mEntry = mEntry;
        }

        @Override
        public K getKey() {
            return mEntry.getKey();
        }

        @Override
        public V getValue() {
            return mEntry.getValue();
        }

        @NonNull
        @Override
        public V setValue(Object object) {
            throw new UnsupportedOperationException();
        }
    }

}
