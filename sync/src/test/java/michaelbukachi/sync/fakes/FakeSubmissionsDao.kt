package michaelbukachi.sync.fakes

import michaelbukachi.data.submissions.Submission
import michaelbukachi.data.submissions.SubmissionsDao

class FakeSubmissionsDao : SubmissionsDao {
    var submissions: List<Submission> = emptyList()
    override fun totalSubmissionsInTheLastNDays(days: Int): Int {
        TODO("Not yet implemented")
    }

    override fun submissionsFromTheLastNDays(days: Int): List<Submission> {
        return submissions
    }

    override fun getTotalCount(): Int {
        return submissions.size
    }

    override fun getLatestSubmission(): Submission? {
        return submissions.lastOrNull()
    }

    override fun getFirstSubmission(): Submission? {
        return submissions.firstOrNull()
    }
}
