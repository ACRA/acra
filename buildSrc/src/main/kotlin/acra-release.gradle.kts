plugins {
    id("fr.brouillard.oss.gradle.jgitver")
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
    }
}

tasks.register<Delete>("clean") {
    group = "build"
    delete = setOf(buildDir)
}