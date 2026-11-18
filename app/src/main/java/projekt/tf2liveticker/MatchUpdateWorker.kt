package projekt.tf2liveticker

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class MatchUpdateWorker(
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
            val matchData = storageRepository.getMatch(matchId)
                ?: return Result.failure()
            if (matchData.selected) {
                repository.refreshResults(matchData, applicationContext)
                storageRepository.updateMatch(matchData)

                if (!matchData.matchEnded) {
                    repository.scheduleMatch(applicationContext, matchData.match)
                }
            }
            Result.success()
        } catch (e: Exception) {

            e.printStackTrace()
            NotificationHelper.showDebugNotification(
                context = applicationContext,
                title = "Error",
                message = e.message.toString(),
                notificationId = 1
            )

            Result.retry()
        }
    }

}