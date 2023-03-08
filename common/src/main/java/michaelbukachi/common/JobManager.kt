package michaelbukachi.common

import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkRequest

class JobManager(context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleJob(request: WorkRequest, cancelExisting: Boolean = true) {
        if (cancelExisting && request.tags.isNotEmpty()) {
            request.tags.forEach {
                workManager.cancelAllWorkByTag(it)
            }
        }
        workManager.cancelAllWorkByTag(SYNC_JOB)
        workManager.enqueue(request)
    }

    companion object {
        const val SYNC_JOB = "SyncJob"
        const val SYNC_JOB_BACKLOG = "SyncJobBacklog"
    }
}
