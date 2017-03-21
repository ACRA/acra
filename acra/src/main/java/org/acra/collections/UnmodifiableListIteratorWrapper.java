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

import java.util.ListIterator;

/**
 * Wrapper around a ListIterator which prevents modifications
 *
 * @author F43nd1r
 * @since 4.9.0
 */
class UnmodifiableListIteratorWrapper<E> implements ListIterator<E> {
    private final ListIterator<E> mIterator;

    UnmodifiableListIteratorWrapper(ListIterator<E> mIterator) {
        this.mIterator = mIterator;
    }

    @Override
    public void add(E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        return mIterator.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return mIterator.hasPrevious();
    }

    @Override
    public E next() {
        return mIterator.next();
    }

    @Override
    public int nextIndex() {
        return mIterator.nextIndex();
    }

    @Override
    public E previous() {
        return mIterator.previous();
    }

    @Override
    public int previousIndex() {
        return mIterator.previousIndex();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(E object) {
        throw new UnsupportedOperationException();
    }
}

