package com.foodcost.ingredient.dto

import com.foodcost.ingredient.entity.Ingredient
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class IngredientDto(
    val id: UUID,
    val name: String,
    val unit: String,
    val price: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(ingredient: Ingredient) = IngredientDto(
            id = ingredient.id!!,
            name = ingredient.name,
            unit = ingredient.unit,
            price = ingredient.price,
            createdAt = ingredient.createdAt,
            updatedAt = ingredient.updatedAt,
        )
    }
}
