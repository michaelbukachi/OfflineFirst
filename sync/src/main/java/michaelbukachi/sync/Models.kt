package michaelbukachi.sync

data class SyncMetrics(
    // No of submissions that were synced immediately
    val immediate: Int,
    // Hours of the day with the most successful syncs
    val mostSuccessfulHours: List<Int>,
)
