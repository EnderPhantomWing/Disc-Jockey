plugins {
    id("maven-publish")
    id("com.github.hierynomus.license") version "0.16.1" apply false
    id("net.fabricmc.fabric-loom") version "1.17.17" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.17.17" apply false
    id("com.replaymod.preprocess") version "c5abb4fb12"
    // Gradle Wrapper Neo Docs: https://github.com/Glavo/gradle-wrapper-neo#getting-started
    id("org.glavo.gradle-wrapper-neo") version "0.2.0"
}

preprocess {
    strictExtraMappings = false

    val mc12111     = createNode("1.21.11", 1_21_11,    "official")
    val mc260102    = createNode("26.1.2",  26_01_02,   "official")
    val mc260200    = createNode("26.2",    26_02_00,   "official")

    mc12111     .link(  mc12111,    null)
    mc12111     .link(  mc260102,   file("mappings/mapping-1.21.11-26.1.2.txt"))
    mc260102    .link(  mc260200,   file("mappings/mapping-26.1.2-26.2.txt"))

    // See https://github.com/Fallen-Breath/fabric-mod-template/blob/1d72d77a1c5ce0bf060c2501270298a12adab679/build.gradle#L55-L63
    for (node in getNodes()) {
        findProject(node.project)
            ?.ext
            ?.set("mcVersion", node.mcVersion)
    }
}

tasks.register("buildAndGather") {
    description = ""
    subprojects {
        dependsOn(tasks.named("build"))
    }
    doFirst {
        println("Gathering builds")
        val buildLibs = { p: Project -> p.layout.buildDirectory.dir("libs").get().asFile.toPath() }
        delete(fileTree(buildLibs(rootProject)) { include("*") })
        subprojects {
            copy {
                from(buildLibs(project)) {
                    include("*.jar")
                    exclude("*-dev.jar", "*-sources.jar", "*-shadow.jar")
                }
                into(buildLibs(rootProject))
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }
}