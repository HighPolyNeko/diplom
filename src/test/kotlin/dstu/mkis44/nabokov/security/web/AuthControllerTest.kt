package dstu.mkis44.nabokov.security.web

import dstu.mkis44.nabokov.exception.InvalidCredentialsException
import dstu.mkis44.nabokov.exception.InvalidTokenException
import dstu.mkis44.nabokov.exception.UserAlreadyExistsException
import dstu.mkis44.nabokov.security.model.*
import dstu.mkis44.nabokov.security.service.AuthService
import dstu.mkis44.nabokov.security.service.TokenService
import dstu.mkis44.nabokov.security.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.junit.jupiter.api.BeforeEach
import java.util.*

@WebMvcTest(
    controllers = [AuthController::class],
    excludeAutoConfiguration = [UserDetailsServiceAutoConfiguration::class]
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest @Autowired constructor(
    private val webApplicationContext: WebApplicationContext
) {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var authService: AuthService

    @MockBean
    private lateinit var authenticationManager: AuthenticationManager

    @MockBean
    private lateinit var tokenService: TokenService

    @MockBean(name = "accessTokenSerializer")
    private lateinit var accessTokenSerializer: Function1<AccessToken, String>

    @MockBean(name = "refreshTokenSerializer")
    private lateinit var refreshTokenSerializer: Function1<RefreshToken, String>

    @Test
    fun `register - should create new user and return tokens`() {
        // given
        val username = "newuser"
        val password = "Password123!"
        val email = "test@example.com"
        val request = RegisterRequest(username = username, password = password, email = email)
        val response = AuthResponse(
            accessToken = "Bearer access_token_value",
            refreshToken = "Bearer refresh_token_value",
            tokenType = "Bearer"
        )

        whenever(userService.findByUsername(username)).thenReturn(null)
        whenever(authService.register(request)).thenReturn(response)

        // when/then
        mockMvc.perform(
            post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"$username","password":"$password","email":"$email"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("Bearer access_token_value"))
            .andExpect(jsonPath("$.refreshToken").value("Bearer refresh_token_value"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))

        verify(authService).register(request)
    }

    @Test
    fun `register - should return conflict when username exists`() {
        // given
        val username = "existinguser"
        val password = "Password123!"
        val email = "test@example.com"
        val existingUser = User(
            id = UUID.randomUUID(),
            username = username,
            password = password,
            email = email,
            roles = mutableSetOf(Role.USER),
            enabled = true
        )

        whenever(userService.findByUsername(username)).thenReturn(existingUser)
        whenever(authService.register(any())).thenThrow(UserAlreadyExistsException("Username already exists"))

        // when/then
        mockMvc.perform(
            post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"$username","password":"$password","email":"$email"}""")
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Username already exists"))
    }

    @Test
    fun `register - should return bad request when email is invalid`() {
        // given
        val username = "newuser"
        val password = "Password123!"
        val email = "invalid-email"

        // when/then
        mockMvc.perform(
            post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"$username","password":"$password","email":"$email"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `register - should return bad request when password is too weak`() {
        // given
        val username = "newuser"
        val password = "weak"
        val email = "test@example.com"

        // when/then
        mockMvc.perform(
            post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"$username","password":"$password","email":"$email"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `login - should authenticate user and return tokens`() {
        // given
        val username = "testuser"
        val password = "Password123!"
        val credentials = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        val request = AuthRequest(credentials = "Basic $credentials")
        val response = AuthResponse(
            accessToken = "Bearer access_token_value",
            refreshToken = "Bearer refresh_token_value",
            tokenType = "Bearer"
        )

        whenever(authService.login(request.credentials)).thenReturn(response)

        // when/then
        mockMvc.perform(
            post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"credentials":"Basic $credentials"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("Bearer access_token_value"))
            .andExpect(jsonPath("$.refreshToken").value("Bearer refresh_token_value"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))

        verify(authService).login(request.credentials)
    }

    @Test
    fun `login - should return unauthorized when credentials are invalid`() {
        // given
        val username = "testuser"
        val password = "WrongPass123!"
        val credentials = Base64.getEncoder().encodeToString("$username:$password".toByteArray())

        whenever(authService.login("Basic $credentials")).thenThrow(InvalidCredentialsException())

        // when/then
        mockMvc.perform(
            post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"credentials":"Basic $credentials"}""")
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Неверные учетные данные"))
    }

    @Test
    fun `login - should return bad request when credentials format is invalid`() {
        // given
        val invalidCredentials = "invalid-format"

        // when/then
        mockMvc.perform(
            post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"credentials":"$invalidCredentials"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Неверный формат учетных данных. Должен быть: Basic Base64(username:password)"))

        verify(authService, never()).login(any())
    }

    @Test
    fun `refresh - should return new access token`() {
        // given
        val refreshToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U"
        val response = RefreshResponse(
            accessToken = "Bearer new_access_token_value",
            tokenType = "Bearer"
        )

        whenever(authService.refresh(refreshToken)).thenReturn(response)

        // when/then
        mockMvc.perform(
            post("/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"$refreshToken"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("Bearer new_access_token_value"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))

        verify(authService).refresh(refreshToken)
    }

    @Test
    fun `refresh - should return unauthorized when refresh token is invalid or expired`() {
        // given
        val testCases = listOf(
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.token" to "Недействительный refresh токен",
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxNTE2MjM5MDIyfQ.L7CX5HuG_gQCgF6w7vqfQyK7O3KF9CDk4bOsZen4yxM" to "Срок действия refresh токена истек"
        )

        testCases.forEach { (token, expectedMessage) ->
            whenever(authService.refresh(token)).thenThrow(InvalidTokenException(expectedMessage))

            // when/then
            mockMvc.perform(
                post("/auth/refresh")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"refreshToken":"$token"}""")
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.message").value(expectedMessage))

            verify(authService).refresh(token)
            reset(authService)
        }
    }
} 