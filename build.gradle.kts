plugins {
    kotlin("jvm") version "1.8.20"
    application
    id("com.google.devtools.ksp") version "1.8.20-1.0.10"
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.8.20"))
    }
}


group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    ksp(project(":processor"))
    kspTest(project(":processor"))
    implementation(project(":annotation"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

