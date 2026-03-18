package com.foodcost.auth

import com.foodcost.auth.dto.AuthResponse
import com.foodcost.auth.dto.RegisterRequest
import com.foodcost.auth.service.AuthService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
        response: HttpServletResponse,
    ): ResponseEntity<AuthResponse> {
        val (authResponse, rawRefreshToken) = authService.register(request)

        val cookie = Cookie("refreshToken", rawRefreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/api/v1/auth/refresh"
            maxAge = 30 * 24 * 60 * 60
            setAttribute("SameSite", "Strict")
        }
        response.addCookie(cookie)

        return ResponseEntity.status(201).body(authResponse)
    }
}
