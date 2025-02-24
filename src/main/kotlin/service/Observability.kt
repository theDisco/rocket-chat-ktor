package com.example.service

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.withContext


class Observability(telemetry: OpenTelemetry) {
    val tracer: Tracer = telemetry.getTracer("rocket-chat")
    val meter: Meter = telemetry.getMeter("rocket-chat")
    val logger: Logger = telemetry.logsBridge.get("rocket-chat")
}

suspend fun <T> Tracer.span(name: String, block: suspend () -> T): T {
    val currentContext = Context.current()
    val span = this.spanBuilder(name).setParent(currentContext).startSpan()
    val newContext = currentContext.with(span)

    return withContext(newContext.asContextElement()) {
        try {
            block()
        } finally {
            span.end()
        }
    }
}

fun Meter.increment(name: String, unit: String, value: Long = 1, description: String = "") {
    this.counterBuilder(name)
        .setUnit(unit)
        .setDescription(description)
        .build()
        .add(value)
}

fun Logger.info(body: String, attributes: Attributes = Attributes.empty()) {
    this.logRecordBuilder()
        .setBody(body)
        .setSeverity(Severity.INFO)
        .setSeverityText("INFO")
        .setAllAttributes(attributes)
        .emit()
}