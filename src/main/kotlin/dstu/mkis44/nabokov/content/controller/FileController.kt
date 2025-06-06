package dstu.mkis44.nabokov.content.controller

import dstu.mkis44.nabokov.content.service.FileStorageService
import dstu.mkis44.nabokov.exception.FileNotFoundException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Path
import java.nio.file.Paths

@RestController
@RequestMapping("/content/files")
@Tag(name = "Файлы", description = "API для получения файлов контента")
class FileController(
    @Value("\${content.storage.path}")
    private val storagePath: String,
    private val fileStorageService: FileStorageService
) {
    private val logger = LoggerFactory.getLogger(FileController::class.java)
    private val root: Path = Paths.get(storagePath)

    @Operation(
        summary = "Получение файла",
        description = """
            Возвращает файл контента по его типу и имени.
            
            Поддерживаемые типы файлов:
            - image: изображения (jpg, jpeg, png, gif)
            - video: видео файлы (mp4)
            - audio: аудио файлы (mp3, wav)
            - document: документы (pdf)
            - thumbnails: миниатюры изображений
            
            Content-Type ответа зависит от типа файла:
            - image: image/jpeg, image/png, image/gif
            - video: video/mp4
            - audio: audio/mpeg, audio/wav
            - document: application/pdf
            """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Файл успешно получен"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Файл не найден"
            )
        ]
    )
    @GetMapping("/{type}/{filename}")
    fun getFile(
        @Parameter(description = "Тип файла (image, video, audio, document, thumbnails)")
        @PathVariable type: String,
        @Parameter(description = "Имя файла")
        @PathVariable filename: String
    ): ResponseEntity<Resource> {
        logger.debug("Getting file of type: {} with name: {}", type, filename)
        
        val filePath = "$type/$filename"
        
        // Проверяем существование файла
        if (!fileStorageService.fileExists(filePath)) {
            logger.warn("File not found: {}", filePath)
            throw FileNotFoundException("File not found: $filePath")
        }
        
        val file = root.resolve(type).resolve(filename)
        val resource = UrlResource(file.toUri())

        if (!resource.exists() || !resource.isReadable) {
            logger.error("Could not read the file: {}", filePath)
            throw FileNotFoundException("Could not read the file: $filePath")
        }

        // Определяем Content-Type на основе типа файла и расширения
        val contentType = determineContentType(type, filename)

        logger.debug("Returning file: {} with content type: {}", filePath, contentType)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${resource.filename}\"")
            .body(resource)
    }
    
    /**
     * Определяет Content-Type на основе типа файла и расширения
     * 
     * @param type Тип файла
     * @param filename Имя файла
     * @return Content-Type
     */
    private fun determineContentType(type: String, filename: String): String {
        val extension = filename.substringAfterLast('.', "").lowercase()
        
        return when (type) {
            "image" -> when (extension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                else -> "image/jpeg"
            }
            "video" -> when (extension) {
                "mp4" -> "video/mp4"
                "avi" -> "video/x-msvideo"
                "mov" -> "video/quicktime"
                else -> "video/mp4"
            }
            "audio" -> when (extension) {
                "mp3" -> "audio/mpeg"
                "wav" -> "audio/wav"
                "ogg" -> "audio/ogg"
                else -> "audio/mpeg"
            }
            "document" -> when (extension) {
                "pdf" -> "application/pdf"
                "doc", "docx" -> "application/msword"
                else -> "application/pdf"
            }
            "thumbnails" -> when (extension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                else -> "image/jpeg"
            }
            else -> MediaType.APPLICATION_OCTET_STREAM_VALUE
        }
    }
}

