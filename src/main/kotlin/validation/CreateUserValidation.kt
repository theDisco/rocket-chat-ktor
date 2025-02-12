package com.example.validation

import com.example.UserRequest
import io.ktor.server.plugins.requestvalidation.*

fun RequestValidationConfig.configureUserValidation() {
    validate<UserRequest> { user ->
        when {
            user.name.isBlank() -> ValidationResult.Invalid("Name cannot be empty")
            user.email.isBlank() -> ValidationResult.Invalid("Email cannot be empty")
            else -> ValidationResult.Valid
        }
    }
}
