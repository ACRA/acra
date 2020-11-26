/*
 * Copyright (c) 2020
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
package org.acra.util

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.RequiresApi


/**
 * Creates a new [PersistableBundle] from the specified [Bundle].
 * Will ignore all values that are not persistable, according
 * to [.isPersistableBundleType].
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
fun Bundle.toPersistableBundle(): PersistableBundle {
    val persistableBundle = PersistableBundle()
    for (key in keySet()) {
        val value = this[key]
        if (isPersistableBundleType(value)) {
            persistableBundle.put(key, value)
        }
    }
    return persistableBundle
}

/**
 * Checks if the specified object can be put into a [PersistableBundle].
 *
 * @see [PersistableBundle Implementation](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/PersistableBundle.java.49)
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
private fun isPersistableBundleType(value: Any?): Boolean {
    return value is PersistableBundle ||
            value is Int || value is IntArray ||
            value is Long || value is LongArray ||
            value is Double || value is DoubleArray ||
            value is String || (value is Array<*> && value.isArrayOf<String>()) ||
            value is Boolean || value is BooleanArray
}

/**
 * Attempts to insert the specified key value pair into the specified bundle.
 *
 * @throws IllegalArgumentException if the value type can not be put into the bundle.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
@Throws(IllegalArgumentException::class)
private fun PersistableBundle.put(key: String, value: Any?) {
    requireNotNull(value) { "Unable to determine type of null values" }
    when {
        value is Int -> putInt(key, value)
        value is IntArray -> putIntArray(key, value)
        value is Long -> putLong(key, value)
        value is LongArray -> putLongArray(key, value)
        value is Double -> putDouble(key, value)
        value is DoubleArray -> putDoubleArray(key, value)
        value is String -> putString(key, value as String?)
        value is Array<*> && value.isArrayOf<String>() -> @Suppress("UNCHECKED_CAST")
        putStringArray(key, value as Array<String>)
        value is Boolean -> putBoolean(key, value)
        value is BooleanArray -> putBooleanArray(key, value)
        value is PersistableBundle -> putAll(value)
        else -> throw IllegalArgumentException("Objects of type ${value.javaClass.simpleName} can not be put into a PersistableBundle")
    }
}