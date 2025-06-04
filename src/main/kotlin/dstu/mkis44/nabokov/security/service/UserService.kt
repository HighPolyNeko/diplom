package dstu.mkis44.nabokov.security.service

import dstu.mkis44.nabokov.security.model.Role
import dstu.mkis44.nabokov.security.model.User
import dstu.mkis44.nabokov.security.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found")
    }

    @Transactional(readOnly = true)
    fun getUserById(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User not found") }
    }

    @Transactional(readOnly = true)
    fun getUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw EntityNotFoundException("User not found")
    }

    @Transactional(readOnly = true)
    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    @Transactional
    fun createUser(username: String, password: String, email: String, roles: Set<Role>): User {
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username already exists")
        }

        val user = User(
            username = username,
            password = passwordEncoder.encode(password),
            email = email,
            roles = roles.toMutableSet()
        )

        return userRepository.save(user)
    }

    @Transactional
    fun updateUser(id: UUID, username: String?, password: String?, email: String?): User {
        val user = getUserById(id)

        username?.let {
            if (userRepository.existsByUsername(it) && it != user.username) {
                throw IllegalArgumentException("Username already exists")
            }
            user.setUsername(it)
        }

        password?.let {
            user.setPassword(passwordEncoder.encode(it))
        }

        email?.let {
            user.setEmail(it)
        }

        return userRepository.save(user)
    }

    @Transactional
    fun deleteUser(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw EntityNotFoundException("User not found")
        }
        userRepository.deleteById(id)
    }
} 