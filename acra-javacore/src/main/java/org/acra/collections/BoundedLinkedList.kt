/*
 *  Copyright 2010 Kevin Gaudin
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
package org.acra.collections

import java.util.*

/**
 * A [LinkedList] version with a maximum number of elements. When adding elements to the end of the list, first elements in the list are discarded if the maximum size is reached.
 *
 * @author Kevin Gaudin
 */
class BoundedLinkedList<E>(private val maxSize: Int) : LinkedList<E>() {
    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#add(java.lang.Object)
     */
    override fun add(element: E): Boolean {
        if (size == maxSize) {
            removeFirst()
        }
        return super.add(element)
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#add(int, java.lang.Object)
     */
    override fun add(index: Int, element: E) {
        if (size == maxSize) {
            removeFirst()
        }
        super.add(index, element)
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#addAll(java.util.Collection)
     */
    override fun addAll(elements: Collection<E>): Boolean {
        var collection = elements
        val size = collection.size
        if (size > maxSize) {
            collection = ArrayList(collection).subList(size - maxSize, size)
        }
        val overhead = size + collection.size - maxSize
        if (overhead > 0) {
            removeRange(0, overhead)
        }
        return super.addAll(collection)
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#addAll(int, java.util.Collection)
     */
    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        // int totalNeededSize = size() + collection.size();
        // int overhead = totalNeededSize - maxSize;
        // if(overhead > 0) {
        // removeRange(0, overhead);
        // }
        // return super.addAll(location, collection);
        if (index == size) {
            return super.addAll(index, elements)
        }
        throw UnsupportedOperationException()
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#addFirst(java.lang.Object)
     */
    override fun addFirst(`object`: E) {
        // super.addFirst(object);
        throw UnsupportedOperationException()
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.LinkedList#addLast(java.lang.Object)
     */
    override fun addLast(`object`: E) {
        add(`object`)
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractCollection#toString()
     */
    override fun toString(): String = joinToString()

    override fun offer(o: E): Boolean = add(o)

    override fun offerFirst(e: E): Boolean {
        addFirst(e)
        return true
    }

    override fun offerLast(e: E): Boolean = add(e)

    override fun push(e: E) {
        add(e)
    }
}