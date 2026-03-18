package com.foodcost.auth

import com.foodcost.auth.dto.AuthResponse
import com.foodcost.auth.dto.LoginRequest
import com.foodcost.auth.dto.UserDto
import com.foodcost.auth.service.AuthService
import com.foodcost.auth.service.EmailAlreadyExistsException
import com.foodcost.auth.service.InvalidCredentialsException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authService: AuthService

    @Test
    fun `POST register with valid body returns 201 and AuthResponse without password`() {
        val userId = UUID.randomUUID()
        val authResponse = AuthResponse(
            accessToken = "test.jwt.token",
            user = UserDto(id = userId, email = "chef@example.com", createdAt = Instant.now()),
        )
        whenever(authService.register(any())).thenReturn(authResponse to "raw-refresh-token")

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"chef@example.com","password":"secure123"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.accessToken") { value("test.jwt.token") }
            jsonPath("$.user.id") { value(userId.toString()) }
            jsonPath("$.user.email") { value("chef@example.com") }
            jsonPath("$.user.password") { doesNotExist() }
            jsonPath("$.user.passwordHash") { doesNotExist() }
        }
    }

    @Test
    fun `POST register with duplicate email returns 422 RFC 7807 Problem Detail`() {
        whenever(authService.register(any()))
            .thenThrow(EmailAlreadyExistsException())

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"chef@example.com","password":"secure123"}"""
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$.status") { value(422) }
            jsonPath("$.title") { value("Unprocessable Entity") }
            jsonPath("$.detail") { value("An account with this email already exists") }
        }
    }

    @Test
    fun `POST register with invalid password shorter than 8 chars returns 400`() {
        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"chef@example.com","password":"short"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.fields.password") { exists() }
        }
    }

    @Test
    fun `POST register with invalid email format returns 400`() {
        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"not-an-email","password":"secure123"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.fields.email") { exists() }
        }
    }

    @Test
    fun `POST login with valid credentials returns 200 and AuthResponse with refreshToken cookie`() {
        val userId = UUID.randomUUID()
        val authResponse = AuthResponse(
            accessToken = "login.jwt.token",
            user = UserDto(id = userId, email = "chef@example.com", createdAt = Instant.now()),
        )
        whenever(authService.login(any())).thenReturn(authResponse to "raw-login-refresh-token")

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"chef@example.com","password":"secure123"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("login.jwt.token") }
            jsonPath("$.user.id") { value(userId.toString()) }
            jsonPath("$.user.email") { value("chef@example.com") }
            cookie { value("refreshToken", "raw-login-refresh-token") }
            cookie { httpOnly("refreshToken", true) }
        }
    }

    @Test
    fun `POST login with wrong password returns 401 RFC 7807 Problem Detail`() {
        whenever(authService.login(any())).thenThrow(InvalidCredentialsException())

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"chef@example.com","password":"wrongpass"}"""
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.status") { value(401) }
            jsonPath("$.title") { value("Unauthorized") }
            jsonPath("$.detail") { value("Invalid credentials") }
        }
    }

    @Test
    fun `POST login with non-existent email returns 401 with same message as wrong password`() {
        whenever(authService.login(any())).thenThrow(InvalidCredentialsException())

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"noone@example.com","password":"secure123"}"""
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.status") { value(401) }
            jsonPath("$.detail") { value("Invalid credentials") }
        }
    }

    @Test
    fun `POST login with blank email returns 400`() {
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"","password":"secure123"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
        }
    }
}
