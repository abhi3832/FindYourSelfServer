
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // Postgres DB
    implementation("org.postgresql:postgresql:42.7.3")

    // Exposed ORM (Optional but highly recommended)
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")

    // Connection pooling (recommended)
    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("com.twilio.sdk:twilio:9.14.1")

    implementation("io.lettuce:lettuce-core:6.7.1.RELEASE")
    implementation("io.ktor:ktor-server-websockets:3.1.3")

}
