package com.biding.exception

import com.biding.db.exception.DuplicatePhoneNumberException
import com.biding.generated.model.ApiError
import com.biding.generated.model.ValidationError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicatePhoneNumberException::class)
    fun handleDuplicatePhoneNumberException(
        ex: DuplicatePhoneNumberException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.CONFLICT.value(),
            error = "DUPLICATE_PHONE_NUMBER",
            message = ex.message,
            path = request.getDescription(false).substring(4), // Remove "uri=" prefix
            timestamp = OffsetDateTime.now(ZoneOffset.UTC)
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val errors = ex.bindingResult.fieldErrors.map { error ->
            ValidationError(
                field = error.field,
                error = error.defaultMessage ?: "Invalid value"
            )
        }

        val error = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "VALIDATION_ERROR",
            message = "Validation failed",
            path = request.getDescription(false).substring(4),
            timestamp = OffsetDateTime.now(ZoneOffset.UTC),
            details = errors
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val error = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred",
            path = request.getDescription(false).substring(4),
            timestamp = OffsetDateTime.now(ZoneOffset.UTC)
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
