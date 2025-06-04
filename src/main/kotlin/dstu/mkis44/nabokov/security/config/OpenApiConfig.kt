package dstu.mkis44.nabokov.security.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"
        return OpenAPI()
            .info(
                Info()
                    .title("API Системы управления контентом")
                    .version("1.0")
                    .description(
                        """
                        REST API для системы управления контентом.
                        
                        ## Аутентификация
                        Для доступа к защищенным эндпоинтам необходимо использовать JWT токен.
                        
                        1. Получите токен через `/auth/login` или `/auth/register`
                        2. Используйте полученный токен в заголовке Authorization: Bearer <token>
                        3. Для обновления токена используйте `/auth/refresh`
                        
                        ## Основные возможности
                        - Регистрация и аутентификация пользователей
                        - Управление контентом
                        - Управление метаданными
                        """.trimIndent()
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList(securitySchemeName)
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description(
                                """
                                JWT токен должен быть получен через эндпоинты аутентификации.
                                Передавайте токен в заголовке Authorization в формате:
                                Bearer <token>
                                """.trimIndent()
                            )
                    )
            )
    }
} 