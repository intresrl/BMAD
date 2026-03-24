package com.foodcost.ingredient.repository

import com.foodcost.ingredient.entity.Ingredient
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IngredientRepository : JpaRepository<Ingredient, UUID> {
    fun findByTenantIdOrderByCreatedAtDesc(tenantId: UUID): List<Ingredient>
    fun existsByTenantIdAndNameIgnoreCase(tenantId: UUID, name: String): Boolean
    fun findByIdAndTenantId(id: UUID, tenantId: UUID): Ingredient?
}
