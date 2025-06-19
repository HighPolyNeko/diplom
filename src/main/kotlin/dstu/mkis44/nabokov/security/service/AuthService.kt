package dstu.mkis44.nabokov.security.service

import com.nimbusds.jose.crypto.DirectDecrypter
import com.nimbusds.jwt.EncryptedJWT
import dstu.mkis44.nabokov.exception.*
import dstu.mkis44.nabokov.security.model.*
import dstu.mkis44.nabokov.security.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.text.ParseException
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val tokenService: TokenService,
    private val refreshTokenSerializer: (RefreshToken) -> String,
    private val accessTokenSerializer: (AccessToken) -> String,
    private val refreshTokenDecrypter: DirectDecrypter,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun register(request: RegisterRequest): AuthResponse {
        logger.debug("Starting registration process for user: ${request.username}")

        if (userRepository.existsByUsername(request.username)) {
            logger.error("Username already exists: ${request.username}")
            throw UserAlreadyExistsException("Пользователь с таким именем уже существует")
        }

        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            email = request.email,
            roles = mutableSetOf(Role.USER),
            enabled = true
        )

        logger.debug("Saving new user: ${request.username}")
        userRepository.save(user)

        logger.debug("Authenticating new user: ${request.username}")
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        logger.debug("Creating tokens for new user: ${request.username}")
        val refreshToken = tokenService.createRefreshToken(authentication)
        val accessToken = tokenService.createAccessToken(refreshToken)

        logger.debug("Registration successful for user: ${request.username}")
        return AuthResponse(
            accessToken = "Bearer ${accessTokenSerializer(accessToken)}",
            refreshToken = "Bearer ${refreshTokenSerializer(refreshToken)}"
        )
    }

    fun login(credentials: String): AuthResponse {
        try {
            logger.debug("Starting login process")
            
            if (!credentials.startsWith("Basic ")) {
                logger.error("Invalid credentials format: missing Basic prefix")
                throw InvalidCredentialsException("Неверный формат учетных данных. Должен начинаться с 'Basic '")
            }

            logger.debug("Decoding credentials")
            val decoded = try {
                String(Base64.getDecoder().decode(credentials.substring(6)), Charsets.UTF_8)
            } catch (e: IllegalArgumentException) {
                logger.error("Failed to decode Base64 credentials: ${e.message}")
                throw InvalidCredentialsException("Неверная кодировка Base64")
            }

            logger.debug("Parsing credentials")
            val parts = decoded.split(":", limit = 2)
            if (parts.size != 2) {
                logger.error("Invalid credentials format: missing username or password")
                throw InvalidCredentialsException("Неверный формат учетных данных")
            }
            val (username, password) = parts

            logger.debug("Authenticating user: $username")
            val authentication = try {
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(username, password)
                )
            } catch (e: Exception) {
                logger.error("Authentication failed for user $username: ${e.message}")
                throw InvalidCredentialsException()
            }

            logger.debug("Creating tokens")
            val refreshToken = tokenService.createRefreshToken(authentication)
            val accessToken = tokenService.createAccessToken(refreshToken)

            logger.debug("Login successful for user: $username")
            return AuthResponse(
                accessToken = "Bearer ${accessTokenSerializer(accessToken)}",
                refreshToken = "Bearer ${refreshTokenSerializer(refreshToken)}"
            )
        } catch (e: Exception) {
            logger.error("Login failed: ${e.message}", e)
            throw when (e) {
                is ApiException -> e
                else -> InvalidCredentialsException()
            }
        }
    }

    fun refresh(refreshTokenStr: String): RefreshResponse {
        try {
            if (!refreshTokenStr.startsWith("Bearer ")) {
                throw InvalidTokenException("Неверный тип токена")
            }
            
            val token = refreshTokenStr.substring(7)
            
            // Парсим и расшифровываем JWT токен
            val encryptedJWT = EncryptedJWT.parse(token)
            encryptedJWT.decrypt(refreshTokenDecrypter)
            val claims = encryptedJWT.jwtClaimsSet

            // Проверяем срок действия
            if (claims.expirationTime.before(Date())) {
                throw InvalidTokenException("Срок действия refresh токена истек")
            }

            // Извлекаем данные из токена
            val username = claims.subject
            val authorities = (claims.getClaim("authorities") as List<*>)
                .map { SimpleGrantedAuthority(it.toString()) }

            // Создаем новую аутентификацию
            val authentication = UsernamePasswordAuthenticationToken(username, null, authorities)

            // Создаем новый access token
            val newRefreshToken = tokenService.createRefreshToken(authentication)
            val newAccessToken = tokenService.createAccessToken(newRefreshToken)

            return RefreshResponse(
                accessToken = "Bearer ${accessTokenSerializer(newAccessToken)}"
            )
        } catch (e: ParseException) {
            throw InvalidTokenException("Неверный формат refresh токена")
        } catch (e: Exception) {
            throw InvalidTokenException("Ошибка обработки refresh токена: ${e.message}")
        }
    }
} 