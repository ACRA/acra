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
import com.android.build.gradle.LibraryExtension
import org.jetbrains.dokka.gradle.DokkaTask


plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

tasks.withType<DokkaTask> {
    dokkaSourceSets.configureEach {
        suppressGeneratedFiles.set(false)
    }
}

tasks.register<Jar>("javadocJar") {
    group = "documentation"
    from(tasks["dokkaJavadoc"])
    archiveClassifier.set("javadoc")
}

afterEvaluate {
    tasks.register<Jar>("sourcesJar") {
        group = "documentation"
        from(
            project.extensions.findByType<LibraryExtension>()?.sourceSets?.get("main")?.java?.srcDirs
                ?: project.extensions.getByType<SourceSetContainer>()["main"].allSource
        )
        archiveClassifier.set("sources")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components.findByName("release") ?: components.findByName("java"))

                tasks.findByName("sourcesJar")?.let { artifact(it) }
                tasks.findByName("javadocJar")?.let { artifact(it) }

                pom {
                    name.set("ACRA")
                    description.set("Publishes reports of Android application crashes to an end point.")
                    url.set("https://acra.ch")
                    scm {
                        connection.set("scm:git:https://github.com/ACRA/acra.git")
                        developerConnection.set("scm:git:git@github.com:ACRA/acra.git")
                        url.set("https://github.com/ACRA/acra.git")
                    }
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
            maven {
                name = "GithubPackages"
                url = uri("https://maven.pkg.github.com/ACRA/acra")
                credentials {
                    username = project.findProperty("githubUser") as? String ?: System.getenv("GITHUB_USER")
                    password = project.findProperty("githubPackageKey") as? String ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }

    signing {
        val signingKey = project.findProperty("signingKey") as? String ?: System.getenv("SIGNING_KEY")
        val signingPassword = project.findProperty("signingPassword") as? String ?: System.getenv("SIGNING_PASSWORD")
        this.isRequired = !signingKey.isNullOrBlank()
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}