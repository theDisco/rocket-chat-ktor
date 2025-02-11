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

    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-3.0:2.12.0-alpha")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:1.47.0");
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.47.0");
    implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.30.0-rc.1")
}

//tasks.withType<JavaExec> {
//    environment("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317/")
//}