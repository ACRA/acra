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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Naive (not optimized) implementation of an Immutable Set with reliable, user-specified iteration order.
 *
 * @author F43nd1r
 * @since 4.9.0
 */
public final class ImmutableSet<E> implements Set<E>, Serializable {
    private static final ImmutableSet<Object> EMPTY = new ImmutableSet<>();

    @NonNull
    public static <T> ImmutableSet<T> empty() {
        //noinspection unchecked
        return (ImmutableSet<T>) EMPTY;
    }

    private final Set<E> mSet;

    private ImmutableSet(){
        this.mSet = Collections.emptySet();
    }

    @SafeVarargs
    public ImmutableSet(E... elements) {
        this(Arrays.asList(elements));
    }

    public ImmutableSet(@NonNull Collection<E> collection) {
        this.mSet = new LinkedHashSet<>(collection);
    }

    @Override
    public boolean add(E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object object) {
        return mSet.contains(object);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return mSet.containsAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return mSet.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIteratorWrapper<>(mSet.iterator());
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return mSet.size();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mSet.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] array) {
        //noinspection SuspiciousToArrayCall
        return mSet.toArray(array);
    }

    public static final class Builder<E> {
        private final Set<E> mSet;

        public Builder() {
            mSet = new LinkedHashSet<>();
        }

        public void add(E element) {
            mSet.add(element);
        }

        public ImmutableSet<E> build() {
            return new ImmutableSet<>(mSet);
        }
    }

}
