package com.foodcost.auth

import com.foodcost.auth.dto.RegisterRequest
import com.foodcost.auth.entity.RefreshToken
import com.foodcost.auth.entity.Tenant
import com.foodcost.auth.entity.User
import com.foodcost.auth.repository.RefreshTokenRepository
import com.foodcost.auth.repository.TenantRepository
import com.foodcost.auth.repository.UserRepository
import com.foodcost.auth.service.AuthService
import com.foodcost.auth.service.EmailAlreadyExistsException
import com.foodcost.auth.service.JwtService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val tenantRepository = mockk<TenantRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtService = mockk<JwtService>()
    private val passwordEncoder = mockk<Argon2PasswordEncoder>()

    private val authService = AuthService(
        userRepository = userRepository,
        tenantRepository = tenantRepository,
        refreshTokenRepository = refreshTokenRepository,
        jwtService = jwtService,
        passwordEncoder = passwordEncoder,
    )

    @Test
    fun `register_withValidInput_createsUserAndTenant`() {
        val request = RegisterRequest(email = "chef@example.com", password = "secure123")
        val tenantId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val savedTenant = Tenant(id = tenantId, name = "example.com")
        val savedUser = User(
            id = userId,
            tenantId = tenantId,
            email = request.email,
            passwordHash = "hashed",
        )
        val savedRefreshToken = RefreshToken(
            userId = userId,
            tokenHash = "somehash",
            expiresAt = Instant.now().plusSeconds(3600),
        )

        every { userRepository.existsByEmail(request.email) } returns false
        every { tenantRepository.save(any()) } returns savedTenant
        every { passwordEncoder.encode(request.password) } returns "hashed"
        every { userRepository.save(any()) } returns savedUser
        every { jwtService.generateRefreshToken() } returns "raw-refresh-token"
        every { refreshTokenRepository.save(any()) } returns savedRefreshToken
        every { jwtService.generateAccessToken(savedUser) } returns "access-token-jwt"

        val (authResponse, rawRefreshToken) = authService.register(request)

        assertNotNull(authResponse)
        assertEquals("access-token-jwt", authResponse.accessToken)
        assertEquals(userId, authResponse.user.id)
        assertEquals(request.email, authResponse.user.email)
        assertFalse(authResponse.toString().contains("password"), "Response must not contain password")
        assertEquals("raw-refresh-token", rawRefreshToken)

        verify(exactly = 1) { tenantRepository.save(any()) }
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `register_withDuplicateEmail_throwsEmailAlreadyExistsException`() {
        val request = RegisterRequest(email = "existing@example.com", password = "secure123")

        every { userRepository.existsByEmail(request.email) } returns true

        assertThrows<EmailAlreadyExistsException> {
            authService.register(request)
        }

        verify(exactly = 0) { tenantRepository.save(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `register_refreshTokenIsStoredHashed_neverPlaintext`() {
        val request = RegisterRequest(email = "chef2@example.com", password = "secure123")
        val tenantId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val rawToken = "my-raw-refresh-token"

        val savedTenant = Tenant(id = tenantId, name = "example.com")
        val savedUser = User(id = userId, tenantId = tenantId, email = request.email, passwordHash = "hashed")

        every { userRepository.existsByEmail(any()) } returns false
        every { tenantRepository.save(any()) } returns savedTenant
        every { passwordEncoder.encode(any()) } returns "hashed"
        every { userRepository.save(any()) } returns savedUser
        every { jwtService.generateRefreshToken() } returns rawToken
        val tokenSlot = slot<RefreshToken>()
        every { refreshTokenRepository.save(capture(tokenSlot)) } answers { tokenSlot.captured }
        every { jwtService.generateAccessToken(any()) } returns "access-jwt"

        authService.register(request)

        assertFalse(tokenSlot.captured.tokenHash == rawToken, "Refresh token must be stored hashed, not plaintext")
    }
}
