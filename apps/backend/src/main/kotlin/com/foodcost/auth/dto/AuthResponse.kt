package com.foodcost.auth.dto

data class AuthResponse(
    val accessToken: String,
    val user: UserDto,
)
