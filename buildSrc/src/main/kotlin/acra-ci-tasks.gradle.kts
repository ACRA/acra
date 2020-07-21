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
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.LibraryExtension

tasks.register<Javadoc>("joinedJavadoc") {
    group = "documentation"
    setDestinationDir(file("$buildDir/javadoc"))
    subprojects {
        val tasks = tasks.withType<Javadoc>()
        source += files(*tasks.map { it.source }.toTypedArray()).asFileTree
        classpath += files(*tasks.map { it.classpath }.toTypedArray())
        val path = project.path.let { if (!it.endsWith(Project.PATH_SEPARATOR)) it + Project.PATH_SEPARATOR else it }
        dependsOn(*tasks.flatMap { task -> task.dependsOn.map { "$path$it"}  }.toTypedArray())
        plugins.withType<LibraryPlugin> {
            linksOffline("http://d.android.com/reference", "${android.sdkDirectory.path}/docs/reference")
            android.libraryVariants.find { it.name == "release" }?.apply {
                classpath += javaCompileProvider.get().classpath
            }
        }
    }
}

tasks.register("printVersion") {
    doLast {
        println(version)
    }
}

val Project.android: LibraryExtension get() = this.extensions.getByType(LibraryExtension::class.java)

fun Javadoc.linksOffline(extDocUrl: String, packageListLoc: String) = (options as StandardJavadocDocletOptions).linksOffline(extDocUrl, packageListLoc)