package com.foodcost.config

import com.foodcost.auth.service.EmailAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI
import org.slf4j.LoggerFactory

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(EmailAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleEmailAlreadyExists(e: EmailAlreadyExistsException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "An account with this email already exists",
        ).also {
            it.type = URI.create("https://foodcost.app/errors/email-already-exists")
            it.title = "Unprocessable Entity"
        }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed").also {
            it.type = URI.create("https://foodcost.app/errors/validation-failed")
            it.setProperty(
                "fields",
                e.bindingResult.fieldErrors.associate { err -> err.field to (err.defaultMessage ?: "invalid") },
            )
        }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(e: Exception): ProblemDetail {
        log.error("Unhandled exception", e)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred").also {
            it.type = URI.create("https://foodcost.app/errors/internal-error")
        }
    }
}
