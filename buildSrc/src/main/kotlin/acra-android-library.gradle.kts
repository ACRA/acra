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
    id("com.android.library")
}
apply(plugin = "kotlin-android")
apply(plugin = "kotlin-kapt")
apply(plugin = "kotlin-allopen")
apply(plugin = "acra-base")
apply(plugin = "org.jetbrains.dokka")

android {
    val androidVersion: String by project
    val androidMinVersion: String by project
    compileSdkVersion(Integer.parseInt(androidVersion))
    defaultConfig {
        minSdkVersion(androidMinVersion)
        targetSdkVersion(androidVersion)
        buildConfigField("String", "VERSION_NAME", "\"$version\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    lintOptions {
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
    val androidXTestVersion: String by project
    androidTestImplementation("androidx.test:core:$androidXTestVersion")
    androidTestImplementation("androidx.test:runner:$androidXTestVersion") {
        exclude(group = "org.hamcrest")
    }
    androidTestImplementation("androidx.test:rules:$androidXTestVersion") {
        exclude(group = "org.hamcrest")
    }
    val androidXJunitVersion: String by project
    androidTestImplementation("androidx.test.ext:junit:$androidXJunitVersion") {
        exclude(group = "org.hamcrest")
    }
    val hamcrestVersion: String by project
    androidTestImplementation("org.hamcrest:hamcrest:$hamcrestVersion")
    val autoServiceVersion: String by project
    "kaptTest"("com.google.auto.service:auto-service:$autoServiceVersion")
    testCompileOnly("com.google.auto.service:auto-service-annotations:$autoServiceVersion")
    "kapt"(project(":annotationprocessor"))
    compileOnly(project(":annotations"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}

tasks.register<Jar>("sourcesJar") {
    group = "documentation"
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

tasks.withType<KaptTask> {
    useBuildCache = false
}

tasks.withType<DokkaTask> {
    dokkaSourceSets {
        named("main") {
            noAndroidSdkLink.set(false)
            sourceRoot(File(buildDir,"generated/source/kaptKotlin/release"))
        }
    }
    dependsOn("assembleRelease")
}

tasks.register<Jar>("javadocJar") {
    group = "documentation"
    from(tasks["dokkaJavadoc"])
    archiveClassifier.set("javadoc")
}

afterEvaluate {
    tasks.findByName("publish")?.mustRunAfter("assembleRelease")
}

fun Javadoc.linksOffline(extDocUrl: String, packageListLoc: String) = (options as StandardJavadocDocletOptions).linksOffline(extDocUrl, packageListLoc)

extensions.getByType<AllOpenExtension>().apply {
    annotation("org.acra.annotation.OpenAPI")
}