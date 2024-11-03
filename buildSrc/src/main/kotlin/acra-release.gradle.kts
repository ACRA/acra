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
    org.jetbrains.dokka
    fr.brouillard.oss.gradle.jgitver
    io.github.`gradle-nexus`.`publish-plugin`
}

jgitver {
    regexVersionTag = "acra-([0-9]+(?:\\.[0-9]+){0,2}(?:-[a-zA-Z0-9\\-_]+)?)"
}

tasks.register("build") {
    group = "build"
}

tasks.register("publish") {
    group = "publishing"
    subprojects {
        tasks.findByName("publish")?.let { dependsOn(it) }
        tasks.findByName("publishToSonatype")?.let { dependsOn(it) }
    }
    dependsOn("closeAndReleaseSonatypeStagingRepository")
}

tasks.register<Delete>("clean") {
    group = "build"
    delete = setOf(layout.buildDirectory)
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.findProperty("ossrhUser") as? String ?: System.getenv("OSSRH_USER"))
            password.set(project.findProperty("ossrhPassword") as? String ?: System.getenv("OSSRH_PASSWORD"))
        }
    }
}