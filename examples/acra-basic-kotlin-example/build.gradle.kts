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
plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
}

android {
    namespace = "org.acra.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.acra.example"
        minSdk = 16
    }
    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    val acraVersion = "5.13.1"
    implementation("ch.acra:acra-http:$acraVersion")

    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}
