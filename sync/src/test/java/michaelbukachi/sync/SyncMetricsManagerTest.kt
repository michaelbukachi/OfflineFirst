package michaelbukachi.sync

import io.mockk.every
import io.mockk.mockk
import michaelbukachi.common.DateUtils
import michaelbukachi.data.submissions.Submission
import michaelbukachi.data.sync.SyncJob
import michaelbukachi.sync.fakes.FakeSubmissionsDao
import michaelbukachi.sync.fakes.FakeSyncJobDao
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.threeten.bp.LocalDateTime

class SyncMetricsManagerTest {

    @Test
    fun testLastSyncIsOlderThanADay_noSubmission() {
        val syncMetrics = SyncMetricsManager(FakeSyncJobDao(), FakeSubmissionsDao(), DateUtils())
        assertThat(syncMetrics.lastSyncIsOlderThanADay(), `is`(false))
    }

    @Test
    fun testLastSyncIsOlderThanADay_submissionExists_lessThan24Hours() {
        val dateUtilsMock = mockk<DateUtils>()
        every { dateUtilsMock.now() } returns LocalDateTime.of(2023, 3, 5, 12, 0, 8)
        val fakeSubmissionsDao = FakeSubmissionsDao()
        fakeSubmissionsDao.submissions = listOf(
            Submission(
                id = 1,
                synced = false,
                syncedAt = null,
                createdAt = LocalDateTime.of(2023, 3, 5, 10, 0, 8),
            ),
        )
        val syncMetrics = SyncMetricsManager(FakeSyncJobDao(), fakeSubmissionsDao, dateUtilsMock)
        assertThat(syncMetrics.lastSyncIsOlderThanADay(), `is`(false))
    }

    @Test
    fun testLastSyncIsOlderThanADay_submissionExists_moreThan24Hours() {
        val dateUtilsMock = mockk<DateUtils>()
        every { dateUtilsMock.now() } returns LocalDateTime.of(2023, 3, 6, 12, 0, 8)
        val fakeSubmissionsDao = FakeSubmissionsDao()
        fakeSubmissionsDao.submissions = listOf(
            Submission(
                id = 1,
                synced = false,
                syncedAt = null,
                createdAt = LocalDateTime.of(2023, 3, 4, 13, 0, 8),
            ),
        )
        val syncMetrics = SyncMetricsManager(FakeSyncJobDao(), fakeSubmissionsDao, dateUtilsMock)
        assertThat(syncMetrics.lastSyncIsOlderThanADay(), `is`(true))
    }

    @Test
    fun testLastSyncIsOlderThanADay_diffBetweenSubmissionAndSync_moreThan24Hours() {
        val fakeSubmissionsDao = FakeSubmissionsDao()
        fakeSubmissionsDao.submissions = listOf(
            Submission(
                id = 1,
                synced = false,
                syncedAt = null,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
            ),
        )
        val fakeSyncJobDao = FakeSyncJobDao()
        fakeSyncJobDao.job = SyncJob(
            id = 1,
            startedAt = LocalDateTime.of(2023, 3, 4, 10, 0, 8),
            finishedAt = null,
        )
        val syncMetrics = SyncMetricsManager(fakeSyncJobDao, fakeSubmissionsDao, DateUtils())
        assertThat(syncMetrics.lastSyncIsOlderThanADay(), `is`(true))
    }

    @Test
    fun testLastSyncIsOlderThanADay_diffBetweenSubmissionAndSync_lessThan24Hours() {
        val fakeSubmissionsDao = FakeSubmissionsDao()
        fakeSubmissionsDao.submissions = listOf(
            Submission(
                id = 1,
                synced = false,
                syncedAt = null,
                createdAt = LocalDateTime.of(2023, 3, 5, 10, 0, 8),
            ),
        )
        val fakeSyncJobDao = FakeSyncJobDao()
        fakeSyncJobDao.job = SyncJob(
            id = 1,
            startedAt = LocalDateTime.of(2023, 3, 4, 12, 0, 8),
            finishedAt = null,
        )
        val syncMetrics = SyncMetricsManager(fakeSyncJobDao, fakeSubmissionsDao, DateUtils())
        assertThat(syncMetrics.lastSyncIsOlderThanADay(), `is`(false))
    }

    @Test
    fun testHighSuccessRate_noSubmission() {
        val fakeSubmissionsDao = FakeSubmissionsDao()
        val syncMetrics = SyncMetricsManager(FakeSyncJobDao(), fakeSubmissionsDao, DateUtils())
        assertThat(syncMetrics.highSuccessRate(), `is`(true))
    }

    @Test
    fun testHighSuccessRate_lessThan48Hours() {
        val dateUtilsMock = mockk<DateUtils>()
        every { dateUtilsMock.now() } returns LocalDateTime.of(2023, 3, 6, 12, 0, 8)
        val fakeSubmissionsDao = FakeSubmissionsDao()
        fakeSubmissionsDao.submissions = listOf(
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = false,
                syncedAt = null,
            ),
        )
        val syncMetrics = SyncMetricsManager(FakeSyncJobDao(), fakeSubmissionsDao, dateUtilsMock)
        assertThat(syncMetrics.highSuccessRate(), `is`(true))
    }

    @Test
    fun testHighSuccessRate_true() {
        val dateUtilsMock = mockk<DateUtils>()
        every { dateUtilsMock.now() } returns LocalDateTime.of(2023, 3, 6, 12, 0, 8)
        val fakeSubmissionsDao = FakeSubmissionsDao()
        fakeSubmissionsDao.submissions = listOf(
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 3, 12, 0, 8),
                synced = true,
                syncedAt = null,
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = true,
                syncedAt = null,
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = true,
                syncedAt = null,
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = true,
                syncedAt = null,
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = false,
                syncedAt = null,
            ),
        )
        val syncMetrics = SyncMetricsManager(FakeSyncJobDao(), fakeSubmissionsDao, dateUtilsMock)
        assertThat(syncMetrics.highSuccessRate(), `is`(true))
    }

    @Test
    fun testHighSuccessRate_false() {
        val dateUtilsMock = mockk<DateUtils>()
        every { dateUtilsMock.now() } returns LocalDateTime.of(2023, 3, 6, 12, 0, 8)
        val fakeSubmissionsDao = FakeSubmissionsDao()
        fakeSubmissionsDao.submissions = listOf(
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 3, 12, 0, 8),
                synced = true,
                syncedAt = null,
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = true,
                syncedAt = null,
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = true,
                syncedAt = null,
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = false,
                syncedAt = null,
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = false,
                syncedAt = null,
            ),
        )
        val syncMetrics = SyncMetricsManager(FakeSyncJobDao(), fakeSubmissionsDao, dateUtilsMock)
        assertThat(syncMetrics.highSuccessRate(), `is`(false))
    }

    @Test
    fun testGetMetrics() {
        val dateUtilsMock = mockk<DateUtils>()
        every { dateUtilsMock.now() } returns LocalDateTime.of(2023, 3, 6, 12, 0, 8)
        val fakeSubmissionsDao = FakeSubmissionsDao()
        fakeSubmissionsDao.submissions = listOf(
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 3, 12, 0, 8),
                synced = true,
                syncedAt = LocalDateTime.of(2023, 3, 3, 12, 3, 8),
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 12, 0, 8),
                synced = true,
                syncedAt = LocalDateTime.of(2023, 3, 3, 12, 3, 8),
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 13, 0, 8),
                synced = true,
                syncedAt = LocalDateTime.of(2023, 3, 5, 14, 0, 8),
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 13, 0, 8),
                synced = true,
                syncedAt = LocalDateTime.of(2023, 3, 5, 14, 0, 8),
            ),
            Submission(
                id = 1,
                createdAt = LocalDateTime.of(2023, 3, 5, 13, 0, 8),
                synced = true,
                syncedAt = LocalDateTime.of(2023, 3, 5, 14, 0, 8),
            ),
        )
        val syncMetrics = SyncMetricsManager(FakeSyncJobDao(), fakeSubmissionsDao, dateUtilsMock)
        assertThat(syncMetrics.getMetrics(), `is`(SyncMetrics(2, listOf(14))))
    }
}
