package dstu.mkis44.nabokov.content.model.dto

import dstu.mkis44.nabokov.content.model.FileType
import java.time.Instant
import java.util.*

data class ContentResponse(
    val id: UUID,
    val title: String,
    val description: String?,
    val authorId: UUID,
    val authorUsername: String,
    val thumbnailUrl: String?,
    val fileType: FileType,
    val fileUrl: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ContentCreateRequest(
    val title: String,
    val description: String?,
    val fileType: FileType
)

data class ContentUpdateRequest(
    val title: String?,
    val description: String?
) 