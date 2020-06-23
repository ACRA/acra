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
    java
}

dependencies {
    implementation(platform(project(":platform")))
    annotationProcessor(platform(project(":platform")))
    implementation("com.google.auto.service:auto-service")
    annotationProcessor("com.google.auto.service:auto-service")
    compileOnly("com.google.auto.service:auto-service-annotations")
    implementation("com.squareup:javapoet:1.11.1")
    implementation("org.apache.commons:commons-lang3:3.8.1")
    implementation("org.apache.commons:commons-text:1.6")
    implementation(project(":annotations"))
    implementation(project(mapOf("path" to  ":acra-javacore", "configuration" to "default")))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}