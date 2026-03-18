package com.foodcost.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

@Configuration
class PasswordEncoderConfig {

    @Bean
    fun passwordEncoder(): Argon2PasswordEncoder =
        Argon2PasswordEncoder(
            /* saltLength  = */ 16,
            /* hashLength  = */ 32,
            /* parallelism = */ 4,
            /* memory      = */ 65536,
            /* iterations  = */ 3,
        )
}
