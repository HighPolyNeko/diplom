package dstu.mkis44.nabokov.content.repository

import dstu.mkis44.nabokov.content.model.Content
import dstu.mkis44.nabokov.content.model.FileType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ContentRepository : JpaRepository<Content, UUID> {
    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<Content>
    fun findByFileType(fileType: FileType, pageable: Pageable): Page<Content>
    fun findByTitleContainingIgnoreCase(title: String, pageable: Pageable): Page<Content>
} 