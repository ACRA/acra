plugins {
    id("com.faendir.gradle.release")
    id("com.jfrog.bintray")
}

release {
    tagTemplate = "acra-$version"
    git {
        pushToRemote = "ACRA"
        requireBranch = "master"
    }
}
tasks["afterReleaseBuild"].dependsOn("publish")

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