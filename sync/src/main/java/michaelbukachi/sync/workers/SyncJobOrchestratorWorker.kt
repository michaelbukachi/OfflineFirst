package michaelbukachi.sync.workers

import android.content.Context
import androidx.work.*
import michaelbukachi.common.DateUtils
import michaelbukachi.common.JobManager
import michaelbukachi.sync.SyncMetricsManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class SyncJobOrchestratorWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {
    private val syncMetrics: SyncMetricsManager by inject()
    private val jobManager: JobManager by inject()
    private val dateUtils: DateUtils by inject()

    override suspend fun doWork(): Result {
        if (!syncMetrics.highSuccessRate()) {
            val metrics = syncMetrics.getMetrics()
            if (metrics.mostSuccessfulHours.isNotEmpty()) {
                // We assume that work manager will try to execute the task around the same time everyday
                // however, this can change due to battery levels, network connectivity etc
                // The periodic time should change based on the success rate.
                val now = dateUtils.now()
                val currentHour = now.hour
                val mostSuccessfulHour = metrics.mostSuccessfulHours.first()
                val delay = if (currentHour < mostSuccessfulHour) {
                    mostSuccessfulHour - currentHour
                } else {
                    now.plusHours(24).minusHours((currentHour - mostSuccessfulHour).toLong()).hour
                }
                val request = PeriodicWorkRequestBuilder<SyncJobBacklogWorker>(1, TimeUnit.DAYS)
                    .addTag(JobManager.SYNC_JOB_BACKLOG)
                    .setConstraints(
                        Constraints(
                            requiredNetworkType = NetworkType.CONNECTED,
                            requiresBatteryNotLow = true,
                        ),
                    )
                    .setInitialDelay(delay.toLong(), TimeUnit.HOURS)
                    .build()
                jobManager.scheduleJob(request)
            }
        }

        return Result.success()
    }
}
