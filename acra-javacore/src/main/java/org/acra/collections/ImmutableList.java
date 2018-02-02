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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Naive (not optimized) implementation of an Immutable List
 *
 * @author F43nd1r
 * @since 4.9.0
 */
public final class ImmutableList<E> implements List<E>, Serializable {

    private final List<E> mList;

    @SafeVarargs
    public ImmutableList(E... elements) {
        this(Arrays.asList(elements));
    }

    public ImmutableList(@NonNull Collection<E> collection) {
        this.mList = new ArrayList<>(collection);
    }

    @Override
    public void add(int location, E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int location, @NonNull Collection<? extends E> collection) {
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
        return mList.contains(object);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return mList.containsAll(collection);
    }

    @Override
    public E get(int location) {
        return mList.get(location);
    }

    @Override
    public int indexOf(Object object) {
        return mList.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return mList.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIteratorWrapper<>(mList.iterator());
    }

    @Override
    public int lastIndexOf(Object object) {
        return mList.lastIndexOf(object);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new UnmodifiableListIteratorWrapper<>(mList.listIterator());
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(int location) {
        return new UnmodifiableListIteratorWrapper<>(mList.listIterator(location));
    }

    @Override
    public E remove(int location) {
        throw new UnsupportedOperationException();
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
    public E set(int location, E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return mList.size();
    }

    @NonNull
    @Override
    public List<E> subList(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mList.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] array) {
        //noinspection SuspiciousToArrayCall
        return mList.toArray(array);
    }

}
