/*
 * Copyright (c) 2019
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acra.collections

import java.lang.ref.WeakReference
import java.util.*

/**
 * A stack which keeps only weak references
 *
 * @author F43nd1r
 * @since 5.3.0
 */
class WeakStack<T> : AbstractCollection<T>() {
    private val contents: MutableList<WeakReference<T?>> = ArrayList()
    private fun cleanup() {
        for (weakReference in contents) {
            if (weakReference.get() == null) contents.remove(weakReference)
        }
    }

    override val size: Int
        get() {
            cleanup()
            return contents.size
        }

    override fun contains(element: T): Boolean {
        return element?.let {
            for (weakReference in contents) {
                if (element == weakReference.get()) return@let true
            }
            false
        } ?: false
    }

    override fun iterator(): MutableIterator<T?> {
        return WeakIterator(contents.iterator())
    }

    override fun add(t: T?): Boolean {
        return contents.add(WeakReference(t))
    }

    override fun remove(element: T): Boolean {
        if (element != null) {
            for (i in contents.indices) {
                if (element == contents[i].get()) {
                    contents.removeAt(i)
                    return true
                }
            }
        }
        return false
    }

    fun peek(): T {
        for (i in contents.indices.reversed()) {
            val result = contents[i].get()
            if (result != null) return result
        }
        throw EmptyStackException()
    }

    fun pop(): T {
        val result = peek()
        remove(result)
        return result
    }

    override fun clear() {
        contents.clear()
    }

    private class WeakIterator<T>(private val iterator: MutableIterator<WeakReference<T>>) : MutableIterator<T> {
        private var next: T? = null
        override fun hasNext(): Boolean {
            if (next != null) return true
            while (iterator.hasNext()) {
                val t = iterator.next().get()
                if (t != null) {
                    //to ensure next() can't throw after hasNext() returned true, we need to dereference this
                    next = t
                    return true
                }
            }
            return false
        }

        override fun next(): T {
            var result = next
            next = null
            while (result == null) {
                result = iterator.next().get()
            }
            return result
        }

        override fun remove() {
            iterator.remove()
        }
    }
}