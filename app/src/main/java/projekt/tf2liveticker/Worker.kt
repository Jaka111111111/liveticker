package projekt.tf2liveticker

import android.content.Context
import androidx.core.graphics.convertTo
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.minutes

class Worker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val repository = MatchRepository()
    private val storageRepository =
        MatchStorageRepository(applicationContext)

    override suspend fun doWork(): Result {

        val matchId = inputData.getInt(
            "MATCH_ID",
            -1
        )

        if (matchId == -1) {
            return Result.failure()
        }

        return try {

            // Match über Repository/API laden
            val matchData = storageRepository.getMatch(matchId)
                ?: return Result.failure()

            repository.refreshResults(matchData)

            if (matchData.oldLength != matchData.length) {

                matchData.oldLength = matchData.length

                NotificationHelper.showNotification(
                    context = applicationContext,
                    title = "${matchData.match.clan1.name} vs ${matchData.match.clan2.name}",

                    message = "Minute ${(matchData.length / 60)}:${(matchData.length % 60)}  ${matchData.blue_score} - ${matchData.red_score}",
                    notificationId = matchId
                )

            }

            // Match verarbeiten
            storageRepository.updateMatch(matchData)
            repository.scheduleMatch(applicationContext, matchData.match)
            Result.success()

        } catch (e: Exception) {

            e.printStackTrace()

            Result.retry()
        }
    }

}