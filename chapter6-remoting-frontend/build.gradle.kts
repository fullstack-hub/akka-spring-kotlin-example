dependencies {
    implementation(project(":akka-typed-coroutine"))
    implementation("com.typesafe.akka:akka-remote_2.13:2.6.17")
    implementation("com.typesafe.akka:akka-serialization-jackson_2.13:2.6.17")
    testImplementation("com.typesafe.akka:akka-multi-node-testkit_2.13:2.6.17")
}
