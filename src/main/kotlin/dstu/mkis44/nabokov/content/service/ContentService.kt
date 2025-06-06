package dstu.mkis44.nabokov.content.service

import dstu.mkis44.nabokov.content.mapper.ContentMapper
import dstu.mkis44.nabokov.content.model.Content
import dstu.mkis44.nabokov.content.model.FileType
import dstu.mkis44.nabokov.content.model.dto.ContentCreateRequest
import dstu.mkis44.nabokov.content.model.dto.ContentResponse
import dstu.mkis44.nabokov.content.model.dto.ContentUpdateRequest
import dstu.mkis44.nabokov.content.repository.ContentRepository
import dstu.mkis44.nabokov.exception.AccessDeniedException
import dstu.mkis44.nabokov.exception.ResourceNotFoundException
import dstu.mkis44.nabokov.security.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

@Service
class ContentService(
    private val contentRepository: ContentRepository,
    private val userService: UserService,
    private val fileStorageService: FileStorageService,
    private val contentMapper: ContentMapper
) {
    private val logger = LoggerFactory.getLogger(ContentService::class.java)

    /**
     * Получает контент по идентификатору
     * 
     * @param id Идентификатор контента
     * @return DTO с информацией о контенте
     * @throws ResourceNotFoundException если контент не найден
     */
    @Transactional(readOnly = true)
    fun getContent(id: UUID): ContentResponse {
        logger.debug("Getting content with id: {}", id)
        return contentRepository.findById(id)
            .map { contentMapper.toResponse(it) }
            .orElseThrow { ResourceNotFoundException("Content not found with id: $id") }
    }

    /**
     * Получает список всего контента
     * 
     * @param pageable Параметры пагинации
     * @return Страница с DTO контента
     */
    @Transactional(readOnly = true)
    fun getAllContent(pageable: Pageable): Page<ContentResponse> {
        logger.debug("Getting all content with pagination: {}", pageable)
        return contentRepository.findAll(pageable).map { contentMapper.toResponse(it) }
    }

    /**
     * Получает список контента определенного автора
     * 
     * @param authorId Идентификатор автора
     * @param pageable Параметры пагинации
     * @return Страница с DTO контента
     */
    @Transactional(readOnly = true)
    fun getContentByAuthor(authorId: UUID, pageable: Pageable): Page<ContentResponse> {
        logger.debug("Getting content by author id: {} with pagination: {}", authorId, pageable)
        return contentRepository.findByAuthorId(authorId, pageable).map { contentMapper.toResponse(it) }
    }

    /**
     * Получает список контента определенного типа
     * 
     * @param fileType Тип файла
     * @param pageable Параметры пагинации
     * @return Страница с DTO контента
     */
    @Transactional(readOnly = true)
    fun getContentByType(fileType: FileType, pageable: Pageable): Page<ContentResponse> {
        logger.debug("Getting content by file type: {} with pagination: {}", fileType, pageable)
        return contentRepository.findByFileType(fileType, pageable).map { contentMapper.toResponse(it) }
    }

    /**
     * Ищет контент по названию
     * 
     * @param query Поисковый запрос
     * @param pageable Параметры пагинации
     * @return Страница с DTO контента
     */
    @Transactional(readOnly = true)
    fun searchContent(query: String, pageable: Pageable): Page<ContentResponse> {
        logger.debug("Searching content with query: {} and pagination: {}", query, pageable)
        return contentRepository.findByTitleContainingIgnoreCase(query, pageable).map { contentMapper.toResponse(it) }
    }

    /**
     * Создает новый контент
     * 
     * @param request Запрос на создание контента
     * @param file Файл контента
     * @param thumbnail Миниатюра (опционально)
     * @param userId Идентификатор пользователя
     * @return DTO созданного контента
     */
    @Transactional
    fun createContent(
        request: ContentCreateRequest,
        file: MultipartFile,
        thumbnail: MultipartFile?,
        userId: UUID
    ): ContentResponse {
        logger.debug("Creating content with title: {} for user: {}", request.title, userId)
        
        val author = userService.getUserById(userId)
        
        // Сохраняем файл с валидацией
        val filePath = fileStorageService.saveFileWithValidation(file, request.fileType)
        
        // Сохраняем миниатюру, если она предоставлена
        val thumbnailPath = thumbnail?.let { 
            fileStorageService.saveFileWithValidation(it, FileType.IMAGE)
        }

        val content = Content(
            title = request.title,
            description = request.description,
            author = author,
            thumbnailPath = thumbnailPath,
            fileType = request.fileType,
            filePath = filePath
        )

        val savedContent = contentRepository.save(content)
        logger.info("Created content with id: {}", savedContent.id)
        
        return contentMapper.toResponse(savedContent)
    }

    /**
     * Обновляет существующий контент
     * 
     * @param id Идентификатор контента
     * @param request Запрос на обновление контента
     * @param userId Идентификатор пользователя
     * @return DTO обновленного контента
     * @throws ResourceNotFoundException если контент не найден
     * @throws AccessDeniedException если пользователь не является автором контента
     */
    @Transactional
    fun updateContent(id: UUID, request: ContentUpdateRequest, userId: UUID): ContentResponse {
        logger.debug("Updating content with id: {} by user: {}", id, userId)
        
        val content = contentRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Content not found with id: $id") }

        // Проверяем, что пользователь является автором контента
        if (content.author.id != userId) {
            logger.warn("User {} attempted to update content {} owned by {}", userId, id, content.author.id)
            throw AccessDeniedException("You don't have permission to update this content")
        }

        // Обновляем поля контента
        request.title?.let { content.title = it }
        request.description?.let { content.description = it }
        content.updatedAt = Instant.now()

        val updatedContent = contentRepository.save(content)
        logger.info("Updated content with id: {}", updatedContent.id)
        
        return contentMapper.toResponse(updatedContent)
    }

    /**
     * Удаляет контент
     * 
     * @param id Идентификатор контента
     * @param userId Идентификатор пользователя
     * @throws ResourceNotFoundException если контент не найден
     * @throws AccessDeniedException если пользователь не является автором контента
     */
    @Transactional
    fun deleteContent(id: UUID, userId: UUID) {
        logger.debug("Deleting content with id: {} by user: {}", id, userId)
        
        val content = contentRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Content not found with id: $id") }

        // Проверяем, что пользователь является автором контента
        if (content.author.id != userId) {
            logger.warn("User {} attempted to delete content {} owned by {}", userId, id, content.author.id)
            throw AccessDeniedException("You don't have permission to delete this content")
        }

        // Удаляем файлы
        fileStorageService.deleteFile(content.filePath)
        content.thumbnailPath?.let { fileStorageService.deleteFile(it) }
        
        // Удаляем запись из базы данных
        contentRepository.delete(content)
        logger.info("Deleted content with id: {}", id)
    }
}

