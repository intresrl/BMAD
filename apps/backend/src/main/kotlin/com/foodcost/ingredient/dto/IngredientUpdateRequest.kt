package com.foodcost.ingredient.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class IngredientUpdateRequest(
    @field:NotBlank val name: String,
    @field:NotBlank val unit: String,
    @field:NotNull @field:DecimalMin(value = "0.0001", inclusive = true) val price: BigDecimal,
)
