package com.foodcost.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email
    val email: String,

    @field:Size(min = 8)
    val password: String,
)
