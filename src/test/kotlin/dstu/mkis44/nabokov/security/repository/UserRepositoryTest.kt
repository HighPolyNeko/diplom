package dstu.mkis44.nabokov.security.repository

import dstu.mkis44.nabokov.security.model.Role
import dstu.mkis44.nabokov.security.model.User
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
open class UserRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val userRepository: UserRepository
) {
    @Test
    fun `should find user by username`() {
        // given
        val user = User(
            username = "test",
            password = "password",
            email = "test@example.com",
            roles = mutableSetOf(Role.USER)
        )
        entityManager.merge(user)
        entityManager.flush()

        // when
        val found = userRepository.findByUsername("test")

        // then
        assertNotNull(found)
        assertEquals("test", found.username)
    }

    @Test
    fun `should return null when user not found`() {
        // when
        val found = userRepository.findByUsername("nonexistent")

        // then
        assertNull(found)
    }

    @Test
    fun `should check if username exists`() {
        // given
        val user = User(
            username = "test",
            password = "password",
            email = "test@example.com",
            roles = mutableSetOf(Role.USER)
        )
        entityManager.merge(user)
        entityManager.flush()

        // when
        val exists = userRepository.existsByUsername("test")
        val notExists = userRepository.existsByUsername("nonexistent")

        // then
        assertEquals(true, exists)
        assertEquals(false, notExists)
    }
} 