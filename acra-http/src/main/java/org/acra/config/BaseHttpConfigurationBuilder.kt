/*
 * Copyright (c) 2017
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

import org.acra.annotation.BuilderMethod
import org.acra.annotation.ConfigurationValue
import java.util.*

/**
 * @author F43nd1r
 * @since 01.06.2017
 */
class BaseHttpConfigurationBuilder internal constructor() {
    private val httpHeaders: MutableMap<String, String>

    /**
     * Set custom HTTP headers to be sent by the provided [org.acra.sender.HttpSender]
     * This should be used also by third party senders.
     *
     * @param headers A map associating HTTP header names to their values.
     */
    @BuilderMethod
    fun setHttpHeaders(headers: Map<String, String>) {
        httpHeaders.clear()
        httpHeaders.putAll(headers)
    }

    @ConfigurationValue
    fun httpHeaders(): Map<String, String> {
        return httpHeaders
    }

    init {
        httpHeaders = HashMap()
    }
}