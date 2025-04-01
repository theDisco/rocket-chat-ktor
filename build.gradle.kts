plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.metrics.micrometer)

    // OpenTelemetry dependencies
    implementation(libs.otel.instrumentation.ktor)
    implementation(libs.otel.instrumentation.jdbc)
    implementation(libs.otel.instrumentation.micrometer)
    implementation(libs.otel.opentelemetry.sdk.extension.autoconfigure)
    implementation(libs.otel.opentelemetry.extension.kotlin)
    implementation(libs.otel.opentelemetry.exporter.otlp)
    implementation(libs.otel.opentelemetry.semconv)

    // Database dependencies
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.postgresql.postgresql)
    implementation(libs.hikari)
}
