plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("org.archguard.scanner.gradle.plugin")
}

dependencies {
    api(project(":rule-core"))

    api("com.phodal.chapi:chapi-domain:2.0.0-beta.9")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("org.assertj:assertj-core:3.22.0")
}

archguard {
    message.set("Just trying this gradle plugin...")
}