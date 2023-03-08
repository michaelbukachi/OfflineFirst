package michaelbukachi.data.submissions

import org.threeten.bp.LocalDateTime

data class Submission(
    val id: Int,
    val synced: Boolean,
    val syncedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)
