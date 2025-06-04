package dstu.mkis44.nabokov.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
open class PasswordEncoderConfig {
    @Bean
    open fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
} 