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
@file:Suppress("unused")

package org.acra.ktx

import android.app.Application
import org.acra.ACRA
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.ConfigurationBuilder

inline fun <reified T : ConfigurationBuilder> CoreConfigurationBuilder.getPluginConfigurationBuilder(): T {
    return this.getPluginConfigurationBuilder(T::class.java)
}

fun Application.initAcra(initializer: CoreConfigurationBuilder.() -> Unit = { }) {
    val builder = CoreConfigurationBuilder(this)
    builder.initializer()
    ACRA.init(this, builder)
}

inline fun <reified T : ConfigurationBuilder> CoreConfigurationBuilder.plugin(initializer: T.() -> Unit) {
    this.getPluginConfigurationBuilder<T>().initializer()
}

fun Throwable.sendWithAcra() {
    ACRA.getErrorReporter().handleException(this)
}

fun Throwable.sendSilentlyWithAcra() {
    ACRA.getErrorReporter().handleSilentException(this)
}