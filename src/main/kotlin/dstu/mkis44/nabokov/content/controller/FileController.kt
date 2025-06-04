package dstu.mkis44.nabokov.content.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
    private val storagePath: String
) {
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
        val file = root.resolve(type).resolve(filename)
        val resource = UrlResource(file.toUri())

        if (!resource.exists() || !resource.isReadable) {
            throw RuntimeException("Could not read the file!")
        }

        val contentType = when (type) {
            "image" -> MediaType.IMAGE_JPEG_VALUE
            "video" -> "video/mp4"
            "audio" -> "audio/mpeg"
            "document" -> MediaType.APPLICATION_PDF_VALUE
            else -> MediaType.APPLICATION_OCTET_STREAM_VALUE
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${resource.filename}\"")
            .body(resource)
    }
} 