package dstu.mkis44.nabokov.content.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Service
class FileStorageService(
    @Value("\${content.storage.path}")
    private val storagePath: String
) {
    private val root: Path = Paths.get(storagePath)

    init {
        Files.createDirectories(root)
    }

    fun saveFile(file: MultipartFile, type: String): String {
        val filename = "${UUID.randomUUID()}-${file.originalFilename}"
        val targetPath = root.resolve(type).resolve(filename)
        Files.createDirectories(targetPath.parent)
        Files.copy(file.inputStream, targetPath)
        return "$type/$filename"
    }

    fun deleteFile(path: String) {
        Files.deleteIfExists(root.resolve(path))
    }

    fun getFileUrl(path: String): String {
        return "/api/content/files/$path"
    }
} 