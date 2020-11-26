/*
 * Copyright (c) 2018 the ACRA team
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
package org.acra.processor.element

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.acra.processor.util.Strings

/**
 * @author F43nd1r
 * @since 10.01.2018
 */
interface ConfigElement : Element {

    fun addToConfig(builder: TypeSpec.Builder, constructor: FunSpec.Builder) {
        //add property
        builder.addProperty(PropertySpec.builder(name, type).build())
        constructor.addStatement("%1L = %2L.%1L", name, Strings.PARAM_0)
    }
}