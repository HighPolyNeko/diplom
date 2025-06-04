package dstu.mkis44.nabokov.security.config

import dstu.mkis44.nabokov.security.model.AccessToken
import dstu.mkis44.nabokov.security.model.RefreshToken
import dstu.mkis44.nabokov.security.service.TokenSerializerService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TokenConfig(
    private val tokenSerializerService: TokenSerializerService
) {
    @Bean
    open fun accessTokenSerializer(): (AccessToken) -> String = { token ->
        tokenSerializerService.serializeAccessToken(token)
    }

    @Bean
    open fun refreshTokenSerializer(): (RefreshToken) -> String = { token ->
        tokenSerializerService.serializeRefreshToken(token)
    }
} 