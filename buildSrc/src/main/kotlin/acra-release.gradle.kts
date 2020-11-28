plugins {
    id("com.jfrog.bintray")
}

tasks.register("build") {
    group = "build"
}

tasks.register("publish") {
    group = "publishing"
    dependsOn(tasks["bintrayPublish"])
    subprojects {
        tasks.findByName("publish")?.let { dependsOn(it) }
    }
}

tasks.register<Delete>("clean") {
    group = "build"
    delete = setOf(buildDir)
}