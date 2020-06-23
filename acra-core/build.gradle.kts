/*
 * Copyright (c) 2020
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
    id("com.android.library")
    `maven-publish`
}

android {
    defaultConfig {
        consumerProguardFile("proguard.cfg")
    }
}

dependencies {
    api(platform(project(":platform")))
    api(project(mapOf("path" to  ":acra-javacore", "configuration" to "default")))
    annotationProcessor("com.google.auto.service:auto-service")
    compileOnly("com.google.auto.service:auto-service-annotations")
    annotationProcessor(project(":annotationprocessor"))
    compileOnly(project(":annotations"))
}
