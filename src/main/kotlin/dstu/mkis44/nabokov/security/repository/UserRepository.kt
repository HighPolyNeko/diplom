package dstu.mkis44.nabokov.security.repository

import dstu.mkis44.nabokov.security.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean
} 