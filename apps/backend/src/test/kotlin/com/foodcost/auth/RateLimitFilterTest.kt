package com.foodcost.auth

import com.foodcost.auth.filter.RateLimitFilter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RateLimitFilterTest {

    private lateinit var filter: RateLimitFilter

    @BeforeEach
    fun setUp() {
        filter = RateLimitFilter(maxRequests = 5, windowSeconds = 60)
    }

    @Test
    fun `request within limit passes through`() {
        val request = MockHttpServletRequest("POST", "/api/v1/auth/login")
        request.remoteAddr = "10.0.0.10"
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, MockFilterChain())

        assertEquals(200, response.status)
    }

    @Test
    fun `6th request within window returns 429 with Retry-After header`() {
        repeat(5) {
            val request = MockHttpServletRequest("POST", "/api/v1/auth/login")
            request.remoteAddr = "10.0.0.1"
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, MockFilterChain())
            assertEquals(200, response.status, "Request ${it + 1} should pass")
        }

        val request = MockHttpServletRequest("POST", "/api/v1/auth/login")
        request.remoteAddr = "10.0.0.1"
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, MockFilterChain())

        assertEquals(429, response.status)
        assertEquals("application/problem+json", response.contentType)
        assertNotNull(response.getHeader("Retry-After"), "Retry-After header must be present")
        assertTrue(response.contentAsString.contains("Rate limit exceeded"))
    }

    @Test
    fun `non-auth endpoint is not rate-limited`() {
        repeat(10) {
            val request = MockHttpServletRequest("GET", "/api/v1/warehouse")
            request.remoteAddr = "10.0.0.20"
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, MockFilterChain())
            assertEquals(200, response.status, "Non-auth request ${it + 1} should pass")
        }
    }

    @Test
    fun `different IPs have independent rate limits`() {
        repeat(5) {
            val request = MockHttpServletRequest("POST", "/api/v1/auth/login")
            request.remoteAddr = "10.0.0.3"
            filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())
        }

        val request = MockHttpServletRequest("POST", "/api/v1/auth/login")
        request.remoteAddr = "10.0.0.4"
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, MockFilterChain())
        assertEquals(200, response.status)
    }
}
