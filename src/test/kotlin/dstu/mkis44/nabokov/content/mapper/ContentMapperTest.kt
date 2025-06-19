package dstu.mkis44.nabokov.content.mapper

import dstu.mkis44.nabokov.content.model.Content
import dstu.mkis44.nabokov.content.model.FileType
import dstu.mkis44.nabokov.content.service.FileStorageService
import dstu.mkis44.nabokov.security.model.Role
import dstu.mkis44.nabokov.security.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.*

class ContentMapperTest {

    private lateinit var fileStorageService: FileStorageService
    private lateinit var contentMapper: ContentMapper
    
    @BeforeEach
    fun setUp() {
        fileStorageService = mock()
        contentMapper = ContentMapper(fileStorageService)
    }
    
    @Test
    fun `toResponse should map Content to ContentResponse correctly`() {
        // Arrange
        val userId = UUID.randomUUID()
        val contentId = UUID.randomUUID()
        val now = Instant.now()
        
        val user = User(
            id = userId,
            username = "testuser",
            password = "password",
            email = "test@example.com",
            roles = mutableSetOf(Role.USER)
        )
        
        val content = Content(
            id = contentId,
            title = "Test Content",
            description = "Test Description",
            author = user,
            thumbnailPath = "thumbnails/test.jpg",
            fileType = FileType.IMAGE,
            filePath = "image/test.jpg",
            createdAt = now,
            updatedAt = now
        )
        
        whenever(fileStorageService.getFileUrl("thumbnails/test.jpg")).thenReturn("/api/content/files/thumbnails/test.jpg")
        whenever(fileStorageService.getFileUrl("image/test.jpg")).thenReturn("/api/content/files/image/test.jpg")
        
        // Act
        val response = contentMapper.toResponse(content)
        
        // Assert
        assertEquals(contentId, response.id)
        assertEquals("Test Content", response.title)
        assertEquals("Test Description", response.description)
        assertEquals(userId, response.authorId)
        assertEquals("testuser", response.authorUsername)
        assertEquals("/api/content/files/thumbnails/test.jpg", response.thumbnailUrl)
        assertEquals(FileType.IMAGE, response.fileType)
        assertEquals("/api/content/files/image/test.jpg", response.fileUrl)
        assertEquals(now, response.createdAt)
        assertEquals(now, response.updatedAt)
    }
    
    @Test
    fun `toResponse should handle null thumbnailPath`() {
        // Arrange
        val userId = UUID.randomUUID()
        val contentId = UUID.randomUUID()
        val now = Instant.now()
        
        val user = User(
            id = userId,
            username = "testuser",
            password = "password",
            email = "test@example.com",
            roles = mutableSetOf(Role.USER)
        )
        
        val content = Content(
            id = contentId,
            title = "Test Content",
            description = "Test Description",
            author = user,
            thumbnailPath = null,
            fileType = FileType.IMAGE,
            filePath = "image/test.jpg",
            createdAt = now,
            updatedAt = now
        )
        
        whenever(fileStorageService.getFileUrl("image/test.jpg")).thenReturn("/api/content/files/image/test.jpg")
        
        // Act
        val response = contentMapper.toResponse(content)
        
        // Assert
        assertEquals(contentId, response.id)
        assertEquals(null, response.thumbnailUrl)
        assertEquals("/api/content/files/image/test.jpg", response.fileUrl)
    }
}

