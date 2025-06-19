package dstu.mkis44.nabokov.content.model

import dstu.mkis44.nabokov.security.model.User
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "contents")
class Content(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User,

    @Column(name = "thumbnail_path")
    var thumbnailPath: String? = null,

    @Column(name = "file_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var fileType: FileType,

    @Column(name = "file_path", nullable = false)
    var filePath: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

enum class FileType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    OTHER
} 