package com.foodcost.auth.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class RateLimitFilter(
    @Value("\${app.rate-limit.max-requests:5}") private val maxRequests: Int,
    @Value("\${app.rate-limit.window-seconds:60}") private val windowSeconds: Long,
) : OncePerRequestFilter() {

    private class RateEntry(val count: AtomicInteger, val windowStart: Long)

    private val requestCounts = ConcurrentHashMap<String, RateEntry>()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith("/api/v1/auth/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val clientIp = request.remoteAddr
        val now = System.currentTimeMillis()
        val windowMillis = windowSeconds * 1000

        val entry = requestCounts.compute(clientIp) { _, existing ->
            if (existing == null || now - existing.windowStart > windowMillis) {
                RateEntry(AtomicInteger(1), now)
            } else {
                existing.count.incrementAndGet()
                existing
            }
        }!!

        if (entry.count.get() > maxRequests) {
            val retryAfterSeconds = maxOf(0L, windowSeconds - (now - entry.windowStart) / 1000)
            response.status = 429
            response.contentType = "application/problem+json"
            response.setHeader("Retry-After", retryAfterSeconds.toString())
            response.writer.write(
                """{"type":"https://foodcost.app/errors/rate-limit","title":"Too Many Requests","status":429,"detail":"Rate limit exceeded. Try again later."}""",
            )
            return
        }

        filterChain.doFilter(request, response)
    }
}
