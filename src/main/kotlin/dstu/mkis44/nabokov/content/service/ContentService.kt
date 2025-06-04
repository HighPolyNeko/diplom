package dstu.mkis44.nabokov.content.service

import dstu.mkis44.nabokov.content.model.Content
import dstu.mkis44.nabokov.content.model.FileType
import dstu.mkis44.nabokov.content.model.dto.ContentCreateRequest
import dstu.mkis44.nabokov.content.model.dto.ContentResponse
import dstu.mkis44.nabokov.content.model.dto.ContentUpdateRequest
import dstu.mkis44.nabokov.content.repository.ContentRepository
import dstu.mkis44.nabokov.security.service.UserService
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class ContentService(
    private val contentRepository: ContentRepository,
    private val userService: UserService,
    private val fileStorageService: FileStorageService
) {
    @Transactional(readOnly = true)
    fun getContent(id: UUID): ContentResponse {
        return contentRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { EntityNotFoundException("Content not found") }
    }

    @Transactional(readOnly = true)
    fun getAllContent(pageable: Pageable): Page<ContentResponse> {
        return contentRepository.findAll(pageable).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getContentByAuthor(authorId: UUID, pageable: Pageable): Page<ContentResponse> {
        return contentRepository.findByAuthorId(authorId, pageable).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getContentByType(fileType: FileType, pageable: Pageable): Page<ContentResponse> {
        return contentRepository.findByFileType(fileType, pageable).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun searchContent(query: String, pageable: Pageable): Page<ContentResponse> {
        return contentRepository.findByTitleContainingIgnoreCase(query, pageable).map { it.toResponse() }
    }

    @Transactional
    fun createContent(
        request: ContentCreateRequest,
        file: MultipartFile,
        thumbnail: MultipartFile?,
        userId: UUID
    ): ContentResponse {
        val author = userService.getUserById(userId)
        val filePath = fileStorageService.saveFile(file, request.fileType.name.lowercase())
        val thumbnailPath = thumbnail?.let { fileStorageService.saveFile(it, "thumbnails") }

        val content = Content(
            title = request.title,
            description = request.description,
            author = author,
            thumbnailPath = thumbnailPath,
            fileType = request.fileType,
            filePath = filePath
        )

        return contentRepository.save(content).toResponse()
    }

    @Transactional
    fun updateContent(id: UUID, request: ContentUpdateRequest, userId: UUID): ContentResponse {
        val content = contentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Content not found") }

        if (content.author.id != userId) {
            throw IllegalAccessException("You don't have permission to update this content")
        }

        request.title?.let { content.title = it }
        request.description?.let { content.description = it }

        return contentRepository.save(content).toResponse()
    }

    @Transactional
    fun deleteContent(id: UUID, userId: UUID) {
        val content = contentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Content not found") }

        if (content.author.id != userId) {
            throw IllegalAccessException("You don't have permission to delete this content")
        }

        fileStorageService.deleteFile(content.filePath)
        content.thumbnailPath?.let { fileStorageService.deleteFile(it) }
        contentRepository.delete(content)
    }

    private fun Content.toResponse() = ContentResponse(
        id = id,
        title = title,
        description = description,
        authorId = author.id,
        authorUsername = author.username,
        thumbnailUrl = thumbnailPath?.let { fileStorageService.getFileUrl(it) },
        fileType = fileType,
        fileUrl = fileStorageService.getFileUrl(filePath),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
} 