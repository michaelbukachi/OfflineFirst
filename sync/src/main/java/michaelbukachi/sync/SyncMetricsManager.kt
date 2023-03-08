package michaelbukachi.sync

import michaelbukachi.common.DateUtils
import michaelbukachi.data.submissions.SubmissionsDao
import michaelbukachi.data.sync.SyncJobDao
import org.threeten.bp.Duration

class SyncMetricsManager(
    private val syncJobDao: SyncJobDao,
    private val submissionsDao: SubmissionsDao,
    private val dateUtils: DateUtils,
) {
    fun highSuccessRate(): Boolean {
        /*
        * High success rate is determined by a >= 80% successful
        * syncs in the last 48 hours
        * */
        val submission = submissionsDao.getFirstSubmission() ?: return true

        val now = dateUtils.now()
        val diff = Duration.between(submission.createdAt, now)
        val hours = diff.toHours()
        // We should start determining the success rate after 48 hours have
        // passed
        if (hours < 48) {
            return true
        }
        val submissions = submissionsDao.submissionsFromTheLastNDays(2)
        val syncedCount = submissions.count { it.synced }
        return syncedCount.toFloat() / submissions.size.toFloat() >= 0.8
    }

    fun getMetrics(): SyncMetrics {
        /*
        * Get metrics of syncs from the last 48 hours
        * */
        val submissions = submissionsDao.submissionsFromTheLastNDays(2)
        var immediate = 0
        val mostSuccessful = mutableListOf<Int>()
        val hoursToSyncs = mutableMapOf<Int, Int>().withDefault { 0 }
        submissions.forEach {
            if (it.synced) {
                val diff = Duration.between(it.createdAt, it.syncedAt!!)
                val minutes = diff.toMinutes()
                if (minutes <= 5) {
                    immediate++
                }
                val hour = it.syncedAt!!.hour

                hoursToSyncs[hour] = (hoursToSyncs[hour] ?: 0) + 1
            }
        }
        val hoursToSyncsSorted = hoursToSyncs.toList().sortedByDescending { (_, value) -> value }.toMap()
        for (entry in hoursToSyncsSorted) {
            if (mostSuccessful.isEmpty() || mostSuccessful.last() == entry.key) {
                mostSuccessful.add(entry.key)
            }
        }
        return SyncMetrics(
            immediate = immediate,
            mostSuccessfulHours = mostSuccessful,
        )
    }

    fun lastSyncIsOlderThanADay(): Boolean {
        /*
        * Method to determine whether a notification should be sent to the user
        * telling them to upload their form submissions
        * */
        if (submissionsDao.getTotalCount() > 0) {
            val syncJob = syncJobDao.getLatestSync()
            val submission = submissionsDao.getLatestSubmission()
            return if (syncJob != null) {
                if (submission!!.createdAt > syncJob.startedAt) {
                    val diff = Duration.between(syncJob.startedAt, submission.createdAt)
                    val hours = diff.toHours()
                    return hours >= 24
                }
                false
            } else {
                val now = dateUtils.now()
                val diff = Duration.between(submission!!.createdAt, now)
                val hours = diff.toHours()
                hours >= 24
            }
        }
        return false
    }
}
