package com.foodcost.auth

import com.foodcost.auth.dto.AuthResponse
import com.foodcost.auth.dto.LoginRequest
import com.foodcost.auth.dto.RegisterRequest
import com.foodcost.auth.service.AuthService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
        response: HttpServletResponse,
    ): ResponseEntity<AuthResponse> {
        log.info("POST /api/v1/auth/register called")
        val (authResponse, rawRefreshToken) = authService.register(request)
        addRefreshTokenCookie(response, rawRefreshToken)
        return ResponseEntity.status(201).body(authResponse)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<AuthResponse> {
        log.info("POST /api/v1/auth/login called")
        val (authResponse, rawRefreshToken) = authService.login(request)
        addRefreshTokenCookie(response, rawRefreshToken)
        return ResponseEntity.ok(authResponse)
    }

    private fun addRefreshTokenCookie(response: HttpServletResponse, rawRefreshToken: String) {
        val cookie = Cookie("refreshToken", rawRefreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/api/v1/auth/refresh"
            maxAge = 30 * 24 * 60 * 60
            setAttribute("SameSite", "Strict")
        }
        response.addCookie(cookie)
    }
}
