package dstu.mkis44.nabokov.content.controller

import dstu.mkis44.nabokov.content.model.FileType
import dstu.mkis44.nabokov.content.model.dto.ContentCreateRequest
import dstu.mkis44.nabokov.content.model.dto.ContentResponse
import dstu.mkis44.nabokov.content.model.dto.ContentUpdateRequest
import dstu.mkis44.nabokov.content.service.ContentService
import dstu.mkis44.nabokov.security.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/content")
@Tag(name = "Контент", description = "API для управления контентом (изображения, видео, аудио, документы)")
@SecurityRequirement(name = "bearerAuth")
class ContentController(
    private val contentService: ContentService
) {
    @Operation(
        summary = "Получение контента по ID",
        description = "Возвращает детальную информацию о контенте по его идентификатору"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Контент успешно найден",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ContentResponse::class),
                    examples = [ExampleObject(
                        value = """
                            {
                                "id": "123e4567-e89b-12d3-a456-426614174000",
                                "title": "Пример контента",
                                "description": "Описание контента",
                                "authorId": "123e4567-e89b-12d3-a456-426614174001",
                                "authorUsername": "author",
                                "thumbnailUrl": "/api/content/files/thumbnails/123.jpg",
                                "fileType": "IMAGE",
                                "fileUrl": "/api/content/files/image/123.jpg",
                                "createdAt": "2024-03-20T12:00:00Z",
                                "updatedAt": "2024-03-20T12:00:00Z"
                            }
                        """
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Контент не найден",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """
                            {
                                "status": 404,
                                "error": "Not Found",
                                "message": "Content not found"
                            }
                        """
                    )]
                )]
            )
        ]
    )
    @GetMapping("/{id}")
    fun getContent(@PathVariable id: UUID): ContentResponse {
        return contentService.getContent(id)
    }

    @Operation(
        summary = "Получение списка всего контента",
        description = "Возвращает постраничный список всего доступного контента"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Список контента успешно получен",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """
                            {
                                "content": [
                                    {
                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                        "title": "Пример контента",
                                        "description": "Описание контента",
                                        "authorId": "123e4567-e89b-12d3-a456-426614174001",
                                        "authorUsername": "author",
                                        "thumbnailUrl": "/api/content/files/thumbnails/123.jpg",
                                        "fileType": "IMAGE",
                                        "fileUrl": "/api/content/files/image/123.jpg",
                                        "createdAt": "2024-03-20T12:00:00Z",
                                        "updatedAt": "2024-03-20T12:00:00Z"
                                    }
                                ],
                                "totalElements": 1,
                                "totalPages": 1,
                                "size": 20,
                                "number": 0
                            }
                        """
                    )]
                )]
            )
        ]
    )
    @GetMapping
    fun getAllContent(pageable: Pageable): Page<ContentResponse> {
        return contentService.getAllContent(pageable)
    }

    @Operation(
        summary = "Получение контента по автору",
        description = "Возвращает постраничный список контента определенного автора"
    )
    @GetMapping("/author/{authorId}")
    fun getContentByAuthor(
        @PathVariable authorId: UUID,
        pageable: Pageable
    ): Page<ContentResponse> {
        return contentService.getContentByAuthor(authorId, pageable)
    }

    @Operation(
        summary = "Получение контента по типу файла",
        description = "Возвращает постраничный список контента определенного типа (IMAGE, VIDEO, AUDIO, DOCUMENT)"
    )
    @GetMapping("/type/{fileType}")
    fun getContentByType(
        @PathVariable fileType: FileType,
        pageable: Pageable
    ): Page<ContentResponse> {
        return contentService.getContentByType(fileType, pageable)
    }

    @Operation(
        summary = "Поиск контента",
        description = "Поиск контента по названию (без учета регистра)"
    )
    @GetMapping("/search")
    fun searchContent(
        @Parameter(description = "Поисковый запрос", example = "презентация")
        @RequestParam query: String,
        pageable: Pageable
    ): Page<ContentResponse> {
        return contentService.searchContent(query, pageable)
    }

    @Operation(
        summary = "Создание нового контента",
        description = """
            Загрузка нового контента с метаданными.
            
            Поддерживаемые типы файлов:
            - IMAGE: jpg, jpeg, png, gif
            - VIDEO: mp4, avi, mov
            - AUDIO: mp3, wav, ogg
            - DOCUMENT: pdf, doc, docx
            
            Максимальный размер файла: 100MB
            
            Требования к файлам:
            - Изображения: jpg, jpeg, png, gif
            - Видео: mp4 (h.264)
            - Аудио: mp3, wav
            - Документы: pdf
            
            Требования к миниатюрам:
            - Формат: jpg, jpeg, png
            - Рекомендуемый размер: 300x300px
            """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Контент успешно создан",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ContentResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректные данные запроса",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """
                            {
                                "status": 400,
                                "error": "Bad Request",
                                "message": "Неподдерживаемый тип файла"
                            }
                        """
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Требуется аутентификация"
            )
        ]
    )
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createContent(
        @Parameter(description = "Метаданные контента")
        @RequestPart("request") request: ContentCreateRequest,
        @Parameter(description = "Файл контента")
        @RequestPart("file") file: MultipartFile,
        @Parameter(description = "Миниатюра (опционально)")
        @RequestPart("thumbnail", required = false) thumbnail: MultipartFile?,
        @Parameter(hidden = true)
        @AuthenticationPrincipal user: User
    ): ContentResponse {
        return contentService.createContent(request, file, thumbnail, user.id)
    }

    @Operation(
        summary = "Обновление контента",
        description = "Обновление метаданных контента (только для автора контента)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Контент успешно обновлен"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Нет прав на обновление контента",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        value = """
                            {
                                "status": 403,
                                "error": "Forbidden",
                                "message": "You don't have permission to update this content"
                            }
                        """
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Контент не найден"
            )
        ]
    )
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateContent(
        @PathVariable id: UUID,
        @RequestBody request: ContentUpdateRequest,
        @Parameter(hidden = true)
        @AuthenticationPrincipal user: User
    ): ContentResponse {
        return contentService.updateContent(id, request, user.id)
    }

    @Operation(
        summary = "Удаление контента",
        description = "Удаление контента и связанных файлов (только для автора контента)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Контент успешно удален"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Нет прав на удаление контента"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Контент не найден"
            )
        ]
    )
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteContent(
        @PathVariable id: UUID,
        @Parameter(hidden = true)
        @AuthenticationPrincipal user: User
    ) {
        contentService.deleteContent(id, user.id)
    }
}
 