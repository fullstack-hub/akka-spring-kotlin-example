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

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    jar.enabled = false
    bootJar.enabled = true

    dependencies {
        val akkaVersion = "2.6.17"
        val akkaHttpVersion = "10.2.7"

        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
        implementation("com.lightbend.akka:akka-stream-alpakka-spring-web_2.13:3.0.3")
        implementation("com.typesafe.akka:akka-actor-typed_2.13:$akkaVersion")
        implementation("com.typesafe.akka:akka-stream-typed_2.13:$akkaVersion")
        implementation("com.typesafe.akka:akka-http_2.13:$akkaHttpVersion")
        implementation("com.typesafe.akka:akka-http-spray-json_2.13:$akkaHttpVersion")
        implementation("com.typesafe.akka:akka-http-jackson_2.13:$akkaHttpVersion")
        implementation("com.typesafe.akka:akka-slf4j_2.13:$akkaVersion")
        implementation("ch.qos.logback:logback-classic:1.2.7")
        testImplementation("com.typesafe.akka:akka-actor-testkit-typed_2.13:$akkaVersion")
        testImplementation("junit:junit:4.13.1")
    }
}
