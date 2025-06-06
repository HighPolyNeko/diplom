package dstu.mkis44.nabokov.content.service

import dstu.mkis44.nabokov.content.model.FileType
import dstu.mkis44.nabokov.exception.FileStorageException
import dstu.mkis44.nabokov.exception.InvalidFileTypeException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class FileStorageService(
    @Value("\${content.storage.path}")
    private val storagePath: String
) {
    private val logger = LoggerFactory.getLogger(FileStorageService::class.java)
    private val root: Path = Paths.get(storagePath)
    
    // Допустимые расширения файлов по типам
    private val allowedExtensions = mapOf(
        FileType.IMAGE to listOf("jpg", "jpeg", "png", "gif"),
        FileType.VIDEO to listOf("mp4", "avi", "mov"),
        FileType.AUDIO to listOf("mp3", "wav", "ogg"),
        FileType.DOCUMENT to listOf("pdf", "doc", "docx"),
        FileType.OTHER to listOf("txt", "zip", "rar")
    )
    
    // Максимальные размеры файлов по типам (в байтах)
    private val maxFileSizes = mapOf(
        FileType.IMAGE to 10 * 1024 * 1024L,      // 10 MB
        FileType.VIDEO to 100 * 1024 * 1024L,     // 100 MB
        FileType.AUDIO to 50 * 1024 * 1024L,      // 50 MB
        FileType.DOCUMENT to 20 * 1024 * 1024L,   // 20 MB
        FileType.OTHER to 50 * 1024 * 1024L       // 50 MB
    )

    init {
        try {
            // Создаем корневую директорию, если она не существует
            if (!Files.exists(root)) {
                Files.createDirectories(root)
                logger.info("Created root storage directory: {}", root)
            }
            
            // Создаем поддиректории для каждого типа файлов
            for (type in FileType.values()) {
                val typeDir = root.resolve(type.name.lowercase())
                if (!Files.exists(typeDir)) {
                    Files.createDirectories(typeDir)
                    logger.info("Created directory for file type {}: {}", type, typeDir)
                }
            }
            
            // Создаем директорию для миниатюр
            val thumbnailsDir = root.resolve("thumbnails")
            if (!Files.exists(thumbnailsDir)) {
                Files.createDirectories(thumbnailsDir)
                logger.info("Created thumbnails directory: {}", thumbnailsDir)
            }
        } catch (e: IOException) {
            logger.error("Failed to initialize storage directories", e)
            throw FileStorageException("Failed to initialize storage directories", e)
        }
    }

    /**
     * Сохраняет файл в хранилище
     * 
     * @param file Файл для сохранения
     * @param type Тип файла (имя директории)
     * @return Относительный путь к сохраненному файлу
     */
    fun saveFile(file: MultipartFile, type: String): String {
        try {
            if (file.isEmpty) {
                throw FileStorageException("Failed to store empty file")
            }
            
            // Проверяем, что директория существует
            val targetDir = root.resolve(type)
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir)
            }
            
            // Генерируем уникальное имя файла
            val originalFilename = file.originalFilename ?: "unknown"
            val filename = "${UUID.randomUUID()}-${sanitizeFilename(originalFilename)}"
            val targetPath = targetDir.resolve(filename)
            
            // Копируем файл
            Files.copy(file.inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
            logger.info("Saved file {} to {}", originalFilename, targetPath)
            
            return "$type/$filename"
        } catch (e: IOException) {
            logger.error("Failed to store file", e)
            throw FileStorageException("Failed to store file", e)
        }
    }
    
    /**
     * Сохраняет файл с проверкой типа и размера
     * 
     * @param file Файл для сохранения
     * @param fileType Тип файла (из перечисления FileType)
     * @return Относительный путь к сохраненному файлу
     */
    fun saveFileWithValidation(file: MultipartFile, fileType: FileType): String {
        // Проверяем расширение файла
        val originalFilename = file.originalFilename ?: throw FileStorageException("Original filename is missing")
        val extension = originalFilename.substringAfterLast('.', "").lowercase()
        
        if (!allowedExtensions[fileType]!!.contains(extension)) {
            throw InvalidFileTypeException(
                "File extension .$extension is not allowed for type ${fileType.name}. " +
                "Allowed extensions: ${allowedExtensions[fileType]!!.joinToString(", ")}"
            )
        }
        
        // Проверяем размер файла
        val maxSize = maxFileSizes[fileType]!!
        if (file.size > maxSize) {
            throw FileStorageException(
                "File size exceeds maximum allowed size for ${fileType.name}: " +
                "${file.size} bytes > ${maxSize} bytes"
            )
        }
        
        return saveFile(file, fileType.name.lowercase())
    }

    /**
     * Удаляет файл из хранилища
     * 
     * @param path Относительный путь к файлу
     */
    fun deleteFile(path: String) {
        try {
            val filePath = root.resolve(path)
            Files.deleteIfExists(filePath)
            logger.info("Deleted file: {}", filePath)
        } catch (e: IOException) {
            logger.error("Failed to delete file: {}", path, e)
            throw FileStorageException("Failed to delete file", e)
        }
    }

    /**
     * Возвращает URL для доступа к файлу
     * 
     * @param path Относительный путь к файлу
     * @return URL для доступа к файлу
     */
    fun getFileUrl(path: String): String {
        return "/api/content/files/$path"
    }
    
    /**
     * Проверяет существование файла
     * 
     * @param path Относительный путь к файлу
     * @return true, если файл существует
     */
    fun fileExists(path: String): Boolean {
        return Files.exists(root.resolve(path))
    }
    
    /**
     * Очищает имя файла от недопустимых символов
     * 
     * @param filename Имя файла
     * @return Очищенное имя файла
     */
    private fun sanitizeFilename(filename: String): String {
        // Оставляем только расширение и заменяем все недопустимые символы на дефис
        val extension = filename.substringAfterLast('.', "")
        return if (extension.isNotEmpty()) {
            "file.$extension"
        } else {
            "file"
        }
    }
}

