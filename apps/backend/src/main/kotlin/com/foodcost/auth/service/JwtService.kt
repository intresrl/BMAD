package com.foodcost.auth.service

import com.foodcost.auth.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.issuer}") private val issuer: String,
    @Value("\${app.jwt.access-token-expiry-minutes:15}") private val expiryMinutes: Long,
) {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    }

    fun generateAccessToken(user: User): String = Jwts.builder()
        .subject(user.id!!.toString())
        .issuer(issuer)
        .claim("tenantId", user.tenantId.toString())
        .claim("roles", user.roles)
        .claim("plan", "base")
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + expiryMinutes * 60 * 1000))
        .signWith(signingKey)
        .compact()

    fun generateRefreshToken(): String = java.util.UUID.randomUUID().toString()

    fun validateAccessToken(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
