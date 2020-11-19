package org.acra.util

import android.util.SparseArray

public inline fun <T, R : Any> Iterable<T>.mapNotNullToSparseArray(transform: (T) -> Pair<Int, R>?): SparseArray<R> {
    val destination = SparseArray<R>()
    forEach { element -> transform(element)?.let { (key, value) -> destination.put(key, value) } }
    return destination
}