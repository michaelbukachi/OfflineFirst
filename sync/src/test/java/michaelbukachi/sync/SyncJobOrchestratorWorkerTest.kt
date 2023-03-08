package michaelbukachi.sync

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker.Result
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import michaelbukachi.common.DateUtils
import michaelbukachi.common.JobManager
import michaelbukachi.sync.workers.SyncJobOrchestratorWorker
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import org.threeten.bp.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class SyncJobOrchestratorWorkerTest : KoinTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        ShadowLog.stream = System.out
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testHighSuccessRate_NothingHappens() {
        val syncMetricsManagerMock = mockk<SyncMetricsManager>()
        every { syncMetricsManagerMock.highSuccessRate() } returns true
        startKoin {
            modules(
                module {
                    single {
                        syncMetricsManagerMock
                    }
                    single { DateUtils() }
                    single { JobManager(context) }
                },
            )
        }
        val worker = TestListenableWorkerBuilder<SyncJobOrchestratorWorker>(
            context = context,
        ).build()
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(Result.success()))
        }
    }

    @Test
    fun testLowSuccessRate_jobIsScheduled() {
        val dateUtilsMock = mockk<DateUtils>()
        every { dateUtilsMock.now() } returns LocalDateTime.of(2023, 3, 5, 12, 0, 8)
        val syncMetricsManagerMock = mockk<SyncMetricsManager>()
        every { syncMetricsManagerMock.highSuccessRate() } returns false
        every { syncMetricsManagerMock.getMetrics() } returns SyncMetrics(
            immediate = 1,
            mostSuccessfulHours = listOf(15),
        )
        startKoin {
            modules(
                module {
                    single {
                        syncMetricsManagerMock
                    }
                    single { dateUtilsMock }
                    single { JobManager(context) }
                },
            )
        }
        val worker = TestListenableWorkerBuilder<SyncJobOrchestratorWorker>(
            context = context,
        ).build()
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(Result.success()))
            val manager = WorkManager.getInstance(context)
            val workInfos = manager.getWorkInfosByTag(JobManager.SYNC_JOB_BACKLOG).await()

            assertThat(workInfos.first().tags.contains(JobManager.SYNC_JOB_BACKLOG), `is`(true))
        }
    }
}
