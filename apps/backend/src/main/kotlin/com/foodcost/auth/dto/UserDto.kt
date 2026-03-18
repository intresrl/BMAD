package com.foodcost.auth.dto

import com.foodcost.auth.entity.User
import java.time.Instant
import java.util.UUID

data class UserDto(
    val id: UUID,
    val email: String,
    val createdAt: Instant,
) {
    companion object {
        fun from(user: User) = UserDto(
            id = user.id!!,
            email = user.email,
            createdAt = user.createdAt,
        )
    }
}
