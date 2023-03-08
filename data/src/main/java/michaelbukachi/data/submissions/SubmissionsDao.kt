package michaelbukachi.data.submissions

interface SubmissionsDao {
    fun totalSubmissionsInTheLastNDays(days: Int): Int
    fun submissionsFromTheLastNDays(days: Int): List<Submission>
    fun getTotalCount(): Int
    fun getLatestSubmission(): Submission?
    fun getFirstSubmission(): Submission?
}
