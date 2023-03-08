package michaelbukachi.data.sync

interface SyncJobDao {
    fun totalSyncsInTheLastNDays(days: Int): Int
    fun syncsFromTheLastNDays(days: Int): List<SyncJob>
    fun numberOfSyncsPer3Hours(days: Int): List<Pair<String, Int>>
    fun getLatestSync(): SyncJob?
}
