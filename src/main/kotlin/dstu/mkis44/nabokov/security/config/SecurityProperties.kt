package dstu.mkis44.nabokov.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "security")
data class SecurityProperties(
    val jwt: JwtProperties,
    val roles: RolesProperties,
    val endpoints: EndpointsProperties
) {
    data class JwtProperties(
        val accessToken: TokenProperties,
        val refreshToken: TokenProperties
    ) {
        data class TokenProperties(
            val ttl: Duration,
            val signingKey: String? = null,
            val encryptionKey: String? = null
        )
    }

    data class RolesProperties(
        val default: String,
        val available: List<String>
    )

    data class EndpointsProperties(
        val public: List<String>
    )
} 