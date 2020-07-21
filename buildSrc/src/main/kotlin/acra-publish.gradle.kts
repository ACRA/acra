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
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPom

plugins {
    `maven-publish`
    id("com.jfrog.bintray")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components.findByName("release") ?: components.findByName("java") ?: components.findByName("javaPlatform"))

                tasks.findByName("sourcesJar")?.let { artifact(it) }
                tasks.findByName("javadocJar")?.let { artifact(it) }

                pom {
                    name.set("ACRA")
                    description.set("Publishes reports of Android application crashes to an end point.")
                    url.set("http://acra.ch")
                    scm {
                        connection.set("scm:git:https://github.com/F43nd1r/acra.git")
                        developerConnection.set("scm:git:git@github.com:F43nd1r/acra.git")
                        url.set("https://github.com/F43nd1r/acra.git")
                    }
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("kevin.gaudin")
                            name.set("Kevin Gaudin")
                        }
                        developer {
                            id.set("william.ferguson")
                            name.set("William Ferguson")
                        }
                        developer {
                            id.set("f43nd1r")
                            name.set("Lukas Morawietz")
                        }
                    }
                }
            }
        }
        repositories {
            mavenLocal()
        }
    }
}

bintray {
    val bintrayUser: String? by project
    val bintrayPassword: String? by project
    user = bintrayUser
    key = bintrayPassword
    setPublications("maven")
    publish = true
    pkg.apply {
        repo = "maven"
        userOrg = "acra"
        afterEvaluate {
            val pom = (publishing.publications["maven"] as MavenPublication).pom as DefaultMavenPom
            this@apply.name = pom.name.get()
            websiteUrl = pom.url.get()
            vcsUrl = pom.scm.url.get()
            setLicenses(*pom.licenses.map { it.name.get() }.toTypedArray())
            desc = pom.description.get()
        }
        publicDownloadNumbers = true
        version.apply {
            name = project.version.toString()
            val ossrhUser: String? by project
            val ossrhPassword: String? by project
            if (ossrhUser != null && ossrhPassword != null) {
                mavenCentralSync.apply {
                    sync = true
                    user = ossrhUser
                    password = ossrhPassword
                }
            }
        }
    }
}
tasks["publish"].dependsOn("bintrayUpload")