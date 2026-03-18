package com.foodcost.auth.filter

import com.foodcost.auth.service.JwtService
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.removePrefix("Bearer ")
        try {
            val claims = jwtService.validateAccessToken(token)
            val roles = (claims["roles"] as? String)?.split(",") ?: emptyList()
            val authorities = roles.map { SimpleGrantedAuthority(it.trim()) }
            val auth = UsernamePasswordAuthenticationToken(claims.subject, null, authorities).apply {
                details = mapOf("tenantId" to claims["tenantId"])
            }
            SecurityContextHolder.getContext().authentication = auth
        } catch (e: JwtException) {
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}
