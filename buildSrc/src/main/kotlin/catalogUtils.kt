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

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

/*
VersionCatalog typesafe accessors aren't available in precompiled script plugins - see https://github.com/gradle/gradle/issues/15383
These are workarounds
 */

val Project.libs: LibrariesForLibs
    get() = the()

fun Project.canUseLibs() = name != "gradle-kotlin-dsl-accessors"