package com.foodcost.config

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class SecurityConfigTest {

    @Test
    fun `security config has required annotations`() {
        val configAnnotation = SecurityConfig::class.java.getAnnotation(
            org.springframework.context.annotation.Configuration::class.java
        )
        val securityAnnotation = SecurityConfig::class.java.getAnnotation(
            org.springframework.security.config.annotation.web.configuration.EnableWebSecurity::class.java
        )
        assertNotNull(configAnnotation, "SecurityConfig must be annotated with @Configuration")
        assertNotNull(securityAnnotation, "SecurityConfig must be annotated with @EnableWebSecurity")
    }
}
