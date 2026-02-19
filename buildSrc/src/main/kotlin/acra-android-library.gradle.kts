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
    `android-library`
    `kotlin-allopen`
    com.google.devtools.ksp
    idea
}

android {
    compileSdk = Integer.parseInt(libs.versions.android.target.get())
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        minSdk = Integer.parseInt(libs.versions.android.min.get())
        buildConfigField("String", "VERSION_NAME", "\"$version\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    lint {
        abortOnError = false
    }
    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<Test> {
    systemProperty("robolectric.logging.enabled", true)
}

dependencies {
    ksp(libs.autoDsl.processor)
    compileOnly(libs.autoDsl.annotations)
    ksp(libs.autoService.ksp)
    implementation(libs.autoService.annotations)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

tasks.withType<KaptTask> {
    useBuildCache = false
}

afterEvaluate {
    tasks.findByName("publish")?.mustRunAfter("assembleRelease")
}

extensions.getByType<AllOpenExtension>().apply {
    annotation("org.acra.annotation.OpenAPI")
}

idea {
    module {
        sourceDirs.plusAssign(file("build/generated/ksp/release/kotlin"))
        generatedSourceDirs.plusAssign(file("build/generated/ksp/release/kotlin"))
    }
}