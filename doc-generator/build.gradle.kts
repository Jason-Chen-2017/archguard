plugins {
    id("application")
    kotlin("jvm") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    alias(libs.plugins.serialization)
}

dependencies {
    api(project(":rule-core"))
    api(project(":scanner_cli"))
    api(project(":rule-linter:rule-code"))
    api(project(":rule-linter:rule-sql"))
    api(project(":rule-linter:rule-test"))
    api(project(":rule-linter:rule-webapi"))

    api("org.jetbrains.kotlin:kotlin-compiler:1.6.21")

    testImplementation(libs.bundles.test)

    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("com.github.ajalt.clikt:clikt:3.4.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.30")
}

application {
    mainClass.set("org.archguard.doc.generator.RunnerKt")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "org.archguard.doc.generator.RunnerKt"))
        }
    }
}
