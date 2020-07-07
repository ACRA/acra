/*
 * Copyright (c) 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
include("acra-javacore")
include("annotationprocessor")
include("annotations")
include("acra-http")
include("acra-core")
include("acra-mail")
include("acra-dialog")
include("acra-toast")
include("acra-notification")
include("acra-limiter")
include("acra-advanced-scheduler")
include("acra-core-ktx")
include("platform")

pluginManagement {
    val androidBuildPluginVersion: String by settings
    val releasePluginVersion: String by settings
    val kotlinVersion: String by settings
    val bintrayPluginVersion: String by settings
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
    plugins {
        id("com.faendir.gradle.release") version releasePluginVersion
        kotlin("android") version kotlinVersion
        id("com.jfrog.bintray") version bintrayPluginVersion
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application" || requested.id.id == "com.android.library") {
                useModule("com.android.tools.build:gradle:${androidBuildPluginVersion}")
            }
        }
    }
}
if(file("acratest").exists()) include("acratest")