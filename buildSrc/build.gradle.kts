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
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    val androidBuildPluginVersion: String by project
    implementation("com.android.tools.build:gradle:$androidBuildPluginVersion")
    val kotlinVersion: String by project
    implementation(kotlin("gradle-plugin:$kotlinVersion"))
    implementation(kotlin("allopen:$kotlinVersion"))
    val dokkaVersion: String by project
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
    implementation("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    val jgitverVersion: String by project
    implementation("gradle.plugin.fr.brouillard.oss.gradle:gradle-jgitver-plugin:$jgitverVersion")
    val nexusPublishVersion: String by project
    implementation("io.github.gradle-nexus:publish-plugin:$nexusPublishVersion")
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