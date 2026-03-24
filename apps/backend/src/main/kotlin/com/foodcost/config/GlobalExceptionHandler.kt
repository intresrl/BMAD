package com.foodcost.config

import com.foodcost.auth.service.EmailAlreadyExistsException
import com.foodcost.auth.service.InvalidCredentialsException
import com.foodcost.ingredient.service.DuplicateIngredientException
import com.foodcost.ingredient.service.IngredientNotFoundException
import com.foodcost.ingredient.service.InvalidUnitException
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

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(e: InvalidCredentialsException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials").also {
            it.type = URI.create("https://foodcost.app/errors/invalid-credentials")
            it.title = "Unauthorized"
        }

    @ExceptionHandler(DuplicateIngredientException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleDuplicateIngredient(e: DuplicateIngredientException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "An ingredient with this name already exists in your warehouse",
        ).also {
            it.type = URI.create("https://foodcost.app/errors/duplicate-ingredient")
            it.title = "Unprocessable Entity"
        }

    @ExceptionHandler(InvalidUnitException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleInvalidUnit(e: InvalidUnitException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            e.message ?: "Invalid unit of measure",
        ).also {
            it.type = URI.create("https://foodcost.app/errors/invalid-unit")
            it.title = "Unprocessable Entity"
        }

    @ExceptionHandler(IngredientNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleIngredientNotFound(e: IngredientNotFoundException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Ingredient not found",
        ).also {
            it.type = URI.create("https://foodcost.app/errors/ingredient-not-found")
            it.title = "Not Found"
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
