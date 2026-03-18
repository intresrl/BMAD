package com.foodcost.auth.repository

import com.foodcost.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenHashAndRevokedFalse(hash: String): RefreshToken?
}
