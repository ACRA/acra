import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    google()
    gradlePluginPortal()
}

dependencies {
    val androidBuildPluginVersion: String by project
    implementation("com.android.tools.build:gradle:$androidBuildPluginVersion")
    val bintrayPluginVersion: String by project
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:$bintrayPluginVersion")
    val releasePluginVersion: String by project
    implementation("com.faendir.gradle:gradle-release:$releasePluginVersion")
    val kotlinVersion: String by project
    implementation(kotlin("gradle-plugin:$kotlinVersion"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}