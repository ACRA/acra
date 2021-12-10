/*
 * Copyright (c) 2021
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

package org.acra.config

import android.widget.Toast
import androidx.annotation.IntRange
import com.faendir.kotlin.autodsl.AutoDsl
import org.acra.annotation.AcraDsl
import org.acra.ktx.plus

/**
 * @author F43nd1r
 * @since 02.06.2017
 */
@AutoDsl(dslMarker = AcraDsl::class)
class ToastConfiguration(
    /**
     * enables this plugin
     */
    val enabled: Boolean = true,

    /**
     * toast text triggered when the application crashes
     *
     * @see android.widget.Toast.makeText
     * @since 5.0.0
     */
    val text: String,

    /**
     * One of [android.widget.Toast.LENGTH_LONG] and [android.widget.Toast.LENGTH_SHORT]
     *
     * @see android.widget.Toast.makeText
     * @since 5.0.0
     */
    @IntRange(from = 0, to = 1)
    val length: Int = Toast.LENGTH_LONG,
) : Configuration {
    override fun enabled(): Boolean = enabled
}

fun CoreConfigurationBuilder.toast(initializer: ToastConfigurationBuilder.() -> Unit) {
    pluginConfigurations += ToastConfigurationBuilder().apply(initializer).build()
}
