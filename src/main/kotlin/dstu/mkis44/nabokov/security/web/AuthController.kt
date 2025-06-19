package dstu.mkis44.nabokov.security.web

import dstu.mkis44.nabokov.security.model.*
import dstu.mkis44.nabokov.security.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "API для аутентификации и управления токенами")
class AuthController(
    private val authService: AuthService
) {
    @Operation(
        summary = "Регистрация нового пользователя",
        description = """
            Регистрирует нового пользователя в системе и возвращает токены доступа.
            
            После успешной регистрации пользователь автоматически аутентифицируется,
            и ему выдаются access и refresh токены.
            
            Требования к паролю:
            - Минимум 8 символов
            - Хотя бы одна буква
            - Хотя бы одна цифра
            - Хотя бы один специальный символ (@$!%*#?&)
            
            Требования к имени пользователя:
            - От 3 до 20 символов
            - Только буквы, цифры, дефис и подчеркивание
            
            Требования к email:
            - Корректный формат email адреса
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно зарегистрирован",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AuthResponse::class),
                        examples = [
                            ExampleObject(
                                value = """
                                    {
                                        "accessToken": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "refreshToken": "Bearer eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiZGlyIn0...",
                                        "tokenType": "Bearer"
                                    }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректные данные для регистрации",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """
                                    {
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "Пароль должен содержать минимум 8 символов, включая хотя бы одну букву, одну цифру и один специальный символ"
                                    }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Пользователь с таким именем уже существует",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """
                                    {
                                        "status": 409,
                                        "error": "Conflict",
                                        "message": "Пользователь с таким именем уже существует"
                                    }
                                """
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "Аутентификация пользователя",
        description = """
            Аутентифицирует пользователя по учетным данным в формате Basic Auth.
            
            Формат учетных данных: username:password
            Кодировка: Base64
            Формат отправки: Basic Base64(username:password)
            
            Пример подготовки данных (Java):
            ```java
            String username = "user123";
            String password = "password123!";
            String credentials = username + ":" + password;
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
            ```
            
            Пример запроса:
            ```json
            {
                "credentials": "Basic dXNlcjEyMzpwYXNzd29yZDEyMyE="
            }
            ```
            
            ⚠️ ВАЖНО: Используйте HTTPS для безопасной передачи учетных данных!
            
            После успешной аутентификации вы получите:
            - Access Token (JWT) для доступа к защищенным ресурсам
            - Refresh Token для обновления access token после истечения срока действия
            
            Срок действия токенов:
            - Access Token: 5 минут
            - Refresh Token: 1 час
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Успешная аутентификация",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AuthResponse::class),
                        examples = [
                            ExampleObject(
                                value = """
                                    {
                                        "accessToken": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "refreshToken": "Bearer eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiZGlyIn0...",
                                        "tokenType": "Bearer"
                                    }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Неверные учетные данные",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """
                                    {
                                        "status": 401,
                                        "error": "Unauthorized",
                                        "message": "Неверные учетные данные"
                                    }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректный формат учетных данных",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """
                                    {
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "Неверный формат учетных данных. Должен быть: Basic Base64(username:password)"
                                    }
                                """
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request.credentials)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "Обновление access токена",
        description = """
            Обновляет access токен с помощью действующего refresh токена.
            
            Используйте этот эндпоинт, когда:
            1. Срок действия access токена истек
            2. Вам нужен новый access токен, но refresh токен еще действителен
            
            Процесс обновления:
            1. Отправьте refresh токен в формате "Bearer <token>"
            2. Получите новый access токен
            
            ⚠️ Важные замечания:
            - Refresh токен должен быть действительным (не истекшим)
            - Каждый refresh токен можно использовать только один раз
            - После использования refresh токена он становится недействительным
            - Если refresh токен скомпрометирован, необходимо выполнить повторную аутентификацию
            
            Пример запроса:
            ```json
            {
                "refreshToken": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            }
            ```
            
            Срок действия нового access токена: 5 минут
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Токен успешно обновлен",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = RefreshResponse::class),
                        examples = [
                            ExampleObject(
                                value = """
                                    {
                                        "accessToken": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "tokenType": "Bearer"
                                    }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Недействительный или истекший refresh токен",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """
                                    {
                                        "status": 401,
                                        "error": "Unauthorized",
                                        "message": "Недействительный refresh токен"
                                    }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректный формат refresh токена",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """
                                    {
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "Неверный формат refresh токена. Должен быть: Bearer <token>"
                                    }
                                """
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshResponse> {
        val response = authService.refresh(request.refreshToken)
        return ResponseEntity.ok(response)
    }
} 