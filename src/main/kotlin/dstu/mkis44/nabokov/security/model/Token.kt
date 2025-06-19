package dstu.mkis44.nabokov.security.model

import java.time.Instant
import java.util.*

interface Token {
    val id: UUID
    val subject: String
    val authorities: List<String>
    val createdAt: Instant
    val expiresAt: Instant
} 