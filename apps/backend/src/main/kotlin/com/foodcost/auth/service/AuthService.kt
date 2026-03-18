package com.foodcost.auth.service

import com.foodcost.auth.dto.AuthResponse
import com.foodcost.auth.dto.UserDto
import com.foodcost.auth.entity.RefreshToken
import com.foodcost.auth.entity.Tenant
import com.foodcost.auth.entity.User
import com.foodcost.auth.repository.RefreshTokenRepository
import com.foodcost.auth.repository.TenantRepository
import com.foodcost.auth.repository.UserRepository
import com.foodcost.auth.dto.RegisterRequest
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

import org.slf4j.LoggerFactory

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: Argon2PasswordEncoder,
) {

    private val log = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun register(request: RegisterRequest): Pair<AuthResponse, String> {
        log.info("Registration attempt for email domain: {}", request.email.substringAfter('@'))
        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException()
        }

        val tenantName = request.email.substringAfter('@').ifBlank { "default" }
        val tenant = tenantRepository.save(Tenant(name = tenantName))

        val passwordHash: String = passwordEncoder.encode(request.password)!!
        val user = userRepository.save(
            User(
                tenantId = tenant.id!!,
                email = request.email,
                passwordHash = passwordHash,
            )
        )

        val rawRefreshToken = jwtService.generateRefreshToken()
        val tokenHash = sha256Hex(rawRefreshToken)
        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id!!,
                tokenHash = tokenHash,
                expiresAt = Instant.now().plusSeconds(30L * 24 * 60 * 60),
            )
        )

        val accessToken = jwtService.generateAccessToken(user)
        val authResponse = AuthResponse(accessToken = accessToken, user = UserDto.from(user))
        return authResponse to rawRefreshToken
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
