import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension
import org.jetbrains.kotlin.gradle.internal.KaptTask
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
    id("repositories")
    id("com.android.library")
    kotlin("android")
    id("kotlin-allopen")
    id("auto-service")
}

android {
    compileSdk = Integer.parseInt(libs.versions.android.target.get())
    defaultConfig {
        minSdk = Integer.parseInt(libs.versions.android.min.get())
        targetSdk = compileSdk
        buildConfigField("String", "VERSION_NAME", "\"$version\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    lint {
        isAbortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")
}

tasks.withType<Test> {
    systemProperty("robolectric.logging.enabled", true)
}

dependencies {
    androidTestImplementation(libs.bundles.androidx.test)
    "kapt"(libs.autoDsl.processor)
    compileOnly(libs.autoDsl.annotations)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}

tasks.withType<KaptTask> {
    useBuildCache = false
}

afterEvaluate {
    tasks.withType<DokkaTask> {
        dokkaSourceSets {
            named("main") {
                noAndroidSdkLink.set(false)
                sourceRoot(File(buildDir,"generated/source/kaptKotlin/release"))
            }
        }
        dependsOn("assembleRelease")
    }
    tasks.findByName("publish")?.mustRunAfter("assembleRelease")
}

extensions.getByType<AllOpenExtension>().apply {
    annotation("org.acra.annotation.OpenAPI")
}