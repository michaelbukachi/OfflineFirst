package michaelbukachi.sync.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import michaelbukachi.common.NotificationHelper
import michaelbukachi.sync.SyncMetricsManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CheckLastSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {
    private val syncMetrics: SyncMetricsManager by inject()
    private val notificationHelper: NotificationHelper by inject()

    override suspend fun doWork(): Result {
        if (syncMetrics.lastSyncIsOlderThanADay()) {
            notificationHelper.showNoSyncInAWhileNotification()
        }

        return Result.success()
    }
}
