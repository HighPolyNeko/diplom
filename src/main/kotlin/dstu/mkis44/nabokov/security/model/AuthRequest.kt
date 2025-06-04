package dstu.mkis44.nabokov.security.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class AuthRequest(
    @field:NotBlank(message = "Учетные данные не могут быть пустыми")
    @field:Pattern(
        regexp = "^Basic [A-Za-z0-9+/=]+$",
        message = "Неверный формат учетных данных. Должен быть: Basic Base64(username:password)"
    )
    @Schema(
        description = "Учетные данные в формате Basic Auth (Base64)",
        example = "Basic dXNlcjEyMzpwYXNzd29yZDEyMyE=",
        required = true
    )
    val credentials: String
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh токен не может быть пустым")
    @field:Pattern(
        regexp = "^Bearer [A-Za-z0-9+/_\\-=.]+$",
        message = "Неверный формат refresh токена. Должен быть: Bearer JWE"
    )
    @Schema(
        description = "Refresh токен с префиксом Bearer",
        example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    val refreshToken: String
)

data class AccessTokenRequest(
    @field:NotBlank(message = "Access токен не может быть пустым")
    @field:Pattern(
        regexp = "^Bearer [A-Za-z0-9+/_\\-=.]+$",
        message = "Неверный формат access токена. Должен быть: Bearer JWT"
    )
    @Schema(
        description = "Access токен с префиксом Bearer",
        example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    val accessToken: String
) 