package dstu.mkis44.nabokov.content.mapper

import dstu.mkis44.nabokov.content.model.Content
import dstu.mkis44.nabokov.content.model.dto.ContentResponse
import dstu.mkis44.nabokov.content.service.FileStorageService
import org.springframework.stereotype.Component

/**
 * Маппер для преобразования сущностей контента в DTO и обратно
 */
@Component
class ContentMapper(
    private val fileStorageService: FileStorageService
) {
    /**
     * Преобразует сущность Content в DTO ContentResponse
     * 
     * @param content Сущность контента
     * @return DTO с информацией о контенте
     */
    fun toResponse(content: Content): ContentResponse {
        return ContentResponse(
            id = content.id,
            title = content.title,
            description = content.description,
            authorId = content.author.id,
            authorUsername = content.author.username,
            thumbnailUrl = content.thumbnailPath?.let { fileStorageService.getFileUrl(it) },
            fileType = content.fileType,
            fileUrl = fileStorageService.getFileUrl(content.filePath),
            createdAt = content.createdAt,
            updatedAt = content.updatedAt
        )
    }
}

