package dstu.mkis44.nabokov.content.service

import dstu.mkis44.nabokov.content.model.FileType
import dstu.mkis44.nabokov.exception.FileStorageException
import dstu.mkis44.nabokov.exception.InvalidFileTypeException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FileStorageServiceTest {

    @TempDir
    lateinit var tempDir: Path
    
    private lateinit var fileStorageService: FileStorageService
    
    @BeforeEach
    fun setUp() {
        // Устанавливаем временную директорию для тестов
        fileStorageService = FileStorageService(tempDir.toString())
    }
    
    @AfterEach
    fun tearDown() {
        // Очищаем временную директорию после тестов
        tempDir.toFile().listFiles()?.forEach { it.deleteRecursively() }
    }
    
    @Test
    fun `saveFile should save file and return relative path`() {
        // Arrange
        val fileContent = "test content".toByteArray()
        val mockFile = MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            fileContent
        )
        
        // Act
        val relativePath = fileStorageService.saveFile(mockFile, "document")
        
        // Assert
        assertTrue(relativePath.startsWith("document/"))
        assertTrue(Files.exists(tempDir.resolve(relativePath)))
        assertArrayEquals(fileContent, Files.readAllBytes(tempDir.resolve(relativePath)))
    }
    
    @Test
    fun `saveFileWithValidation should save valid file`() {
        // Arrange
        val fileContent = "test image content".toByteArray()
        val mockFile = MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            fileContent
        )
        
        // Act
        val relativePath = fileStorageService.saveFileWithValidation(mockFile, FileType.IMAGE)
        
        // Assert
        assertTrue(relativePath.startsWith("image/"))
        assertTrue(Files.exists(tempDir.resolve(relativePath)))
    }
    
    @Test
    fun `saveFileWithValidation should throw exception for invalid file type`() {
        // Arrange
        val fileContent = "test text content".toByteArray()
        val mockFile = MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            fileContent
        )
        
        // Act & Assert
        assertThrows(InvalidFileTypeException::class.java) {
            fileStorageService.saveFileWithValidation(mockFile, FileType.IMAGE)
        }
    }
    
    @Test
    fun `deleteFile should remove existing file`() {
        // Arrange
        val fileContent = "test content".toByteArray()
        val mockFile = MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            fileContent
        )
        val relativePath = fileStorageService.saveFile(mockFile, "document")
        
        // Act
        fileStorageService.deleteFile(relativePath)
        
        // Assert
        assertFalse(Files.exists(tempDir.resolve(relativePath)))
    }
    
    @Test
    fun `deleteFile should not throw exception for non-existing file`() {
        // Act & Assert
        assertDoesNotThrow {
            fileStorageService.deleteFile("non-existing/file.txt")
        }
    }
    
    @Test
    fun `getFileUrl should return correct URL`() {
        // Act
        val url = fileStorageService.getFileUrl("image/test.jpg")
        
        // Assert
        assertEquals("/api/content/files/image/test.jpg", url)
    }
    
    @Test
    fun `fileExists should return true for existing file`() {
        // Arrange
        val fileContent = "test content".toByteArray()
        val mockFile = MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            fileContent
        )
        val relativePath = fileStorageService.saveFile(mockFile, "document")
        
        // Act & Assert
        assertTrue(fileStorageService.fileExists(relativePath))
    }
    
    @Test
    fun `fileExists should return false for non-existing file`() {
        // Act & Assert
        assertFalse(fileStorageService.fileExists("non-existing/file.txt"))
    }
}

