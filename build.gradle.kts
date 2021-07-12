import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.kotless.plugin.gradle.dsl.kotless

plugins {
    kotlin("jvm") version "1.5.20"
    id("io.kotless") version "0.1.6" apply true
}

group = "me.n-takehata"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("io.kotless", "kotless-lang", "0.1.6")
    testImplementation(kotlin("test"))
}

kotless {
    //<...>
    extensions {
        local {
            //enable AWS emulation (disabled by default)
            useAWSEmulation = true
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
