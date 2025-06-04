package dstu.mkis44.nabokov.security.model

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

data class RefreshResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
) 