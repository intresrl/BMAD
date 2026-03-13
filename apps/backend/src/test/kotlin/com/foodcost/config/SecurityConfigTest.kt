package com.foodcost.config

import org.junit.jupiter.api.Test
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import kotlin.test.assertNotNull

class SecurityConfigTest {

    @Test
    fun `security config class can be instantiated`() {
        val config = SecurityConfig()
        assertNotNull(config)
    }
}
