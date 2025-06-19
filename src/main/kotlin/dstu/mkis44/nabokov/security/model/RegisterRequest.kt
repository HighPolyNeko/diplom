package dstu.mkis44.nabokov.security.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*

data class RegisterRequest(
    @field:NotBlank(message = "Имя пользователя не может быть пустым")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_-]{3,20}$",
        message = "Имя пользователя должно содержать от 3 до 20 символов и может включать только буквы, цифры, дефис и подчеркивание"
    )
    @Schema(
        description = "Имя пользователя",
        example = "user123",
        required = true
    )
    val username: String,

    @field:NotBlank(message = "Пароль не может быть пустым")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "Пароль должен содержать минимум 8 символов, включая хотя бы одну букву, одну цифру и один специальный символ"
    )
    @Schema(
        description = "Пароль",
        example = "password123!",
        required = true
    )
    val password: String,

    @field:NotBlank(message = "Email не может быть пустым")
    @field:Email(message = "Некорректный формат email")
    @Schema(
        description = "Электронная почта",
        example = "user@example.com",
        required = true
    )
    val email: String
) 