package dstu.mkis44.nabokov.security.model

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    private var username: String,

    @Column(nullable = false)
    private var password: String,

    @Column(nullable = false)
    private var email: String,

    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    var roles: MutableSet<Role> = mutableSetOf(Role.USER),

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    fun getEmail(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled

    fun setPassword(password: String) {
        this.password = password
        this.updatedAt = Instant.now()
    }

    fun setUsername(username: String) {
        this.username = username
        this.updatedAt = Instant.now()
    }

    fun setEmail(email: String) {
        this.email = email
        this.updatedAt = Instant.now()
    }
}

enum class Role {
    USER,
    MODERATOR,
    ADMIN
} 