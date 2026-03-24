package com.foodcost.ingredient

import com.foodcost.ingredient.dto.IngredientCreateRequest
import com.foodcost.ingredient.dto.IngredientDto
import com.foodcost.ingredient.service.IngredientService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/ingredients")
class IngredientController(private val ingredientService: IngredientService) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: IngredientCreateRequest,
        authentication: Authentication,
    ): ResponseEntity<IngredientDto> {
        val tenantId = extractTenantId(authentication)
        val created = ingredientService.create(request, tenantId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping
    fun list(authentication: Authentication): List<IngredientDto> {
        val tenantId = extractTenantId(authentication)
        return ingredientService.findAll(tenantId)
    }

    private fun extractTenantId(authentication: Authentication): UUID {
        val details = authentication.details as? Map<*, *>
            ?: throw IllegalStateException("Missing authentication details")
        val tenantId = details["tenantId"] as? String
            ?: throw IllegalStateException("Missing tenantId in JWT")
        return UUID.fromString(tenantId)
    }
}
