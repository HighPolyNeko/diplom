package dstu.mkis44.nabokov.content.model.dto

import dstu.mkis44.nabokov.content.model.FileType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.*

@Schema(description = "Ответ с информацией о контенте")
data class ContentResponse(
    @Schema(description = "Уникальный идентификатор контента", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,
    
    @Schema(description = "Название контента", example = "Красивый пейзаж")
    val title: String,
    
    @Schema(description = "Описание контента", example = "Фотография горного пейзажа на закате")
    val description: String?,
    
    @Schema(description = "Идентификатор автора", example = "123e4567-e89b-12d3-a456-426614174001")
    val authorId: UUID,
    
    @Schema(description = "Имя пользователя автора", example = "photographer")
    val authorUsername: String,
    
    @Schema(description = "URL миниатюры", example = "/api/content/files/thumbnails/123e4567-e89b-12d3-a456-426614174000.jpg")
    val thumbnailUrl: String?,
    
    @Schema(description = "Тип файла", example = "IMAGE")
    val fileType: FileType,
    
    @Schema(description = "URL файла", example = "/api/content/files/image/123e4567-e89b-12d3-a456-426614174000.jpg")
    val fileUrl: String,
    
    @Schema(description = "Дата создания", example = "2024-03-20T12:00:00Z")
    val createdAt: Instant,
    
    @Schema(description = "Дата последнего обновления", example = "2024-03-20T12:00:00Z")
    val updatedAt: Instant
)

@Schema(description = "Запрос на создание контента")
data class ContentCreateRequest(
    @field:NotBlank(message = "Название не может быть пустым")
    @field:Size(min = 3, max = 100, message = "Название должно содержать от 3 до 100 символов")
    @Schema(description = "Название контента", example = "Красивый пейзаж", required = true)
    val title: String,
    
    @field:Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    @Schema(description = "Описание контента", example = "Фотография горного пейзажа на закате")
    val description: String?,
    
    @field:NotNull(message = "Тип файла должен быть указан")
    @Schema(description = "Тип файла", example = "IMAGE", required = true)
    val fileType: FileType
)

@Schema(description = "Запрос на обновление контента")
data class ContentUpdateRequest(
    @field:Size(min = 3, max = 100, message = "Название должно содержать от 3 до 100 символов")
    @Schema(description = "Новое название контента", example = "Горный пейзаж на закате")
    val title: String?,
    
    @field:Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    @Schema(description = "Новое описание контента", example = "Фотография горного пейзажа на закате в Альпах")
    val description: String?
)

