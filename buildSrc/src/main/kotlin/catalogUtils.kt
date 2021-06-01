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
@file:Suppress("UnstableApiUsage")

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import java.util.*
import kotlin.reflect.KProperty

/*
VersionCatalog typesafe accessors aren't available in precompiled script plugins - see https://github.com/gradle/gradle/issues/15383
These are workarounds
 */

val Project.Libs: VersionCatalog
    get() = the<VersionCatalogsExtension>().named("libs")

class Versions(private val versionCatalog: VersionCatalog) {
    operator fun getValue(receiver: Versions?, property: KProperty<*>): String = get(property.name.replace(Regex("([A-Z])"), "-$1").toLowerCase(Locale.ROOT))

    operator fun get(name: String): String =
        versionCatalog.findVersion(name).orElse(null)?.toString() ?: throw IllegalArgumentException("Unknown version $name")
}

class Bundles(private val versionCatalog: VersionCatalog) {
    operator fun getValue(receiver: Bundles?, property: KProperty<*>): Provider<ExternalModuleDependencyBundle> =
        get(property.name.replace(Regex("([A-Z])"), "-$1").toLowerCase(Locale.ROOT))

    operator fun get(name: String): Provider<ExternalModuleDependencyBundle> =
        versionCatalog.findBundle(name).orElse(null) ?: throw IllegalArgumentException("Unknown version $name")
}

val VersionCatalog.versions: Versions
    get() = Versions(this)

val VersionCatalog.bundles: Bundles
    get() = Bundles(this)

operator fun VersionCatalog.get(library: String): Provider<MinimalExternalModuleDependency> = findDependency(library).get()