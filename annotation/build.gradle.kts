plugins {
    kotlin("jvm") version "1.8.20"
}

group = "tech.dzolotov.sampleksp.annotation"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

