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
package org.acra.processor.util

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.apache.commons.text.WordUtils
import java.io.IOException
import java.text.DateFormat
import java.util.*
import javax.annotation.processing.ProcessingEnvironment

/**
 * @author F43nd1r
 * @since 08.01.2018
 */
object Strings {
    const val PREFIX_RES = "res"
    const val PREFIX_SETTER = "with"
    const val PARAM_0 = "arg0"
    const val VAR_ANNOTATION = "annotation"
    const val FIELD_DELEGATE = "delegate"
    const val FIELD_CONTEXT = "context"
    const val FIELD_ENABLED = "enabled"
    const val PACKAGE = "org.acra.config"
    const val CONTEXT = "android.content.Context"
    const val CONFIGURATION_BUILDER_FACTORY = "org.acra.config.ConfigurationBuilderFactory"
    private val DATE_FORMAT = DateFormat.getDateTimeInstance()
    fun ensurePrefix(prefix: String, value: String): String {
        return if (value.startsWith(prefix)) value else prefix + WordUtils.capitalize(value)
    }

    fun addClassKdoc(builder: TypeSpec.Builder, base: TypeName) {
        builder.addKdoc("Class generated based on [%T] (%L)\n", base, DATE_FORMAT.format(Calendar.getInstance().time))
    }

    /**
     * Writes the given class to a respective file in the configuration package
     *
     * @param filer    filer to write to
     * @param typeSpec the class
     * @throws IOException if writing fails
     */
    @Throws(IOException::class)
    fun writeClass(processingEnv: ProcessingEnvironment, typeSpec: TypeSpec) {
        FileSpec.builder(PACKAGE, typeSpec.name!!)
                .addType(typeSpec)
                .indent("    ")
                .writeTo(processingEnv)
    }
}