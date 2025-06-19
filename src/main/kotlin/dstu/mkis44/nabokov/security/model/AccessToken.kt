package dstu.mkis44.nabokov.security.model

import java.time.Instant
import java.util.*

data class AccessToken(
    override val id: UUID,
    override val subject: String,
    override val authorities: List<String>,
    override val createdAt: Instant,
    override val expiresAt: Instant
) : Token 