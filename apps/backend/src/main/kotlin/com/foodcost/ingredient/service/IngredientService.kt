package com.foodcost.ingredient.service

import com.foodcost.ingredient.dto.IngredientCreateRequest
import com.foodcost.ingredient.dto.IngredientDto
import com.foodcost.ingredient.dto.IngredientUpdateRequest
import com.foodcost.ingredient.entity.Ingredient
import com.foodcost.ingredient.repository.IngredientRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class IngredientService(
    private val ingredientRepository: IngredientRepository,
) {

    companion object {
        val ALLOWED_UNITS = setOf("kg", "g", "hg", "l", "cl", "ml", "pz", "confezione", "porzione")
    }

    @Transactional
    fun create(request: IngredientCreateRequest, tenantId: UUID): IngredientDto {
        if (request.unit !in ALLOWED_UNITS) {
            throw InvalidUnitException(request.unit)
        }

        val trimmedName = request.name.trim()

        if (ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, trimmedName)) {
            throw DuplicateIngredientException()
        }

        val ingredient = ingredientRepository.save(
            Ingredient(
                tenantId = tenantId,
                name = trimmedName,
                unit = request.unit,
                price = request.price,
            )
        )

        return IngredientDto.from(ingredient)
    }

    fun findAll(tenantId: UUID): List<IngredientDto> {
        return ingredientRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
            .map { IngredientDto.from(it) }
    }

    @Transactional
    fun update(id: UUID, request: IngredientUpdateRequest, tenantId: UUID): IngredientDto {
        if (request.unit !in ALLOWED_UNITS) {
            throw InvalidUnitException(request.unit)
        }

        val ingredient = ingredientRepository.findByIdAndTenantId(id, tenantId)
            ?: throw IngredientNotFoundException()

        val trimmedName = request.name.trim()

        if (!ingredient.name.equals(trimmedName, ignoreCase = true)) {
            if (ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, trimmedName)) {
                throw DuplicateIngredientException()
            }
        }

        ingredient.name = trimmedName
        ingredient.unit = request.unit
        ingredient.price = request.price
        ingredient.updatedAt = Instant.now()

        return IngredientDto.from(ingredientRepository.save(ingredient))
    }
}
