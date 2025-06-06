package dstu.mkis44.nabokov.security.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Ответ с токенами аутентификации")
data class AuthResponse(
    @Schema(
        description = "Access токен с префиксом Bearer",
        example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    val accessToken: String,
    
    @Schema(
        description = "Refresh токен с префиксом Bearer",
        example = "Bearer eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiZGlyIn0...",
        required = true
    )
    val refreshToken: String,
    
    @Schema(
        description = "Тип токена",
        example = "Bearer",
        required = true
    )
    val tokenType: String = "Bearer"
)

@Schema(description = "Ответ с обновленным access токеном")
data class RefreshResponse(
    @Schema(
        description = "Новый access токен с префиксом Bearer",
        example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    val accessToken: String,
    
    @Schema(
        description = "Тип токена",
        example = "Bearer",
        required = true
    )
    val tokenType: String = "Bearer"
)

