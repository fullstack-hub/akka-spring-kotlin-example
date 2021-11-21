import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.5.7"
}

group = "com.labs.somnium"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")

    jar.enabled = false
    bootJar.enabled = true

    dependencies {
        val akkaVersion = "2.6.17"
        val scalaVersion = "2.13"
        val akkaHttpVersion = "10.2.7"

        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
        implementation("ch.qos.logback:logback-classic:1.2.7")

        implementation("com.lightbend.akka:akka-stream-alpakka-spring-web_$scalaVersion:3.0.3")
        implementation("com.typesafe.akka:akka-actor-typed_$scalaVersion:$akkaVersion")
        implementation("com.typesafe.akka:akka-stream-typed_$scalaVersion:$akkaVersion")
        implementation("com.typesafe.akka:akka-http_$scalaVersion:$akkaHttpVersion")
        implementation("com.typesafe.akka:akka-http-spray-json_$scalaVersion:$akkaHttpVersion")
        implementation("com.typesafe.akka:akka-http-jackson_$scalaVersion:$akkaHttpVersion")
        implementation("com.typesafe.akka:akka-slf4j_$scalaVersion:$akkaVersion")

        testImplementation("com.typesafe.akka:akka-actor-testkit-typed_$scalaVersion:$akkaVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.6.0")
    }
}
