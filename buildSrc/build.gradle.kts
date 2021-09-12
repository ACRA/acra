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
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.android.build)
    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.allopen)
    implementation(libs.dokka.gradle)
    implementation(libs.dokka.core)
    implementation(libs.jgitver)
    implementation(libs.nexusPublish)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}