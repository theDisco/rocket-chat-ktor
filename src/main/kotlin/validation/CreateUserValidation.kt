package com.example.validation

import com.example.model.CreateUserRequest
import com.example.model.UpdateUserRequest
import io.ktor.server.plugins.requestvalidation.*

fun RequestValidationConfig.configureUserValidation() {
    validate<CreateUserRequest> { user ->
        when {
            user.name.isBlank() -> ValidationResult.Invalid("Name cannot be empty")
            user.email.isBlank() -> ValidationResult.Invalid("Email cannot be empty")
            else -> ValidationResult.Valid
        }
    }

    validate<UpdateUserRequest> { user ->
        when {
            user.name.isBlank() -> ValidationResult.Invalid("Name cannot be empty")
            else -> ValidationResult.Valid
        }
    }
}
