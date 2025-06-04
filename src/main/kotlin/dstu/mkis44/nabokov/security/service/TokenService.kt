package dstu.mkis44.nabokov.security.service

import dstu.mkis44.nabokov.security.model.AccessToken
import dstu.mkis44.nabokov.security.model.RefreshToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
open class TokenService {
    private val refreshTokenTtl = Duration.ofDays(30)
    private val accessTokenTtl = Duration.ofHours(1)

    open fun createRefreshToken(authentication: Authentication): RefreshToken {
        val authorities = mutableListOf<String>()
        
        authentication.authorities
            .map { "GRANT_${it.authority}" }
            .forEach { authorities.add(it) }
            
        authorities.add("JWT_REFRESH")

        val now = Instant.now()
        return RefreshToken(
            id = UUID.randomUUID(),
            subject = authentication.name,
            authorities = authorities,
            createdAt = now,
            expiresAt = now.plus(refreshTokenTtl)
        )
    }

    open fun createAccessToken(refreshToken: RefreshToken): AccessToken {
        val authorities = mutableListOf<String>()
        
        refreshToken.authorities
            .filter { it.startsWith("GRANT_") }
            .map { it.substring("GRANT_".length) }
            .forEach { authorities.add(it) }
            
        if (!authorities.contains("JWT_LOGOUT")) {
            authorities.add("JWT_LOGOUT")
        }

        val now = Instant.now()
        return AccessToken(
            id = UUID.randomUUID(),
            subject = refreshToken.subject,
            authorities = authorities,
            createdAt = now,
            expiresAt = now.plus(accessTokenTtl)
        )
    }
} 