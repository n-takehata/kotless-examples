import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.kotless.plugin.gradle.dsl.kotless

plugins {
    kotlin("jvm") version "1.4.32" apply true
    id("io.kotless") version "0.1.7-beta-5" apply true
}

group = "com.example.kotless.takehata"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("io.kotless", "ktor-lang", "0.1.7-beta-5")
    testImplementation(kotlin("test"))
}

kotless {
    // Change to your AWS configuration
    config {
        // This is a bucket name for Amazon S3
        bucket = "kotless-example-takehata"

        terraform {
            // This is credentials for AWS
            profile = "default"
            region = "us-west-2"
        }
    }

    webapp {
        lambda {
            memoryMb = 1024
            timeoutSec = 120
        }
    }

    extensions {
        terraform {
            allowDestroy = true
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
