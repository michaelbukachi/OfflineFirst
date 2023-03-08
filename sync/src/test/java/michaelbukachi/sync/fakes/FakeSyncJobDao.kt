package michaelbukachi.sync.fakes

import michaelbukachi.data.sync.SyncJob
import michaelbukachi.data.sync.SyncJobDao

class FakeSyncJobDao : SyncJobDao {
    var job: SyncJob? = null
    override fun totalSyncsInTheLastNDays(days: Int): Int {
        TODO("Not yet implemented")
    }

    override fun syncsFromTheLastNDays(days: Int): List<SyncJob> {
        TODO("Not yet implemented")
    }

    override fun numberOfSyncsPer3Hours(days: Int): List<Pair<String, Int>> {
        TODO("Not yet implemented")
    }

    override fun getLatestSync(): SyncJob? {
        return job
    }
}
