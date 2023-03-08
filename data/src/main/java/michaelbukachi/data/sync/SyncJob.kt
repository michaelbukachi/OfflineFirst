package michaelbukachi.data.sync

import org.threeten.bp.LocalDateTime

data class SyncJob(
    val id: Int,
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
)
