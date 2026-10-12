package projekt.tf2liveticker

import android.content.Context
import androidx.work.CoroutineWorker
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

            // Match über Repository/API laden
            val matchData = storageRepository.getMatch(matchId)
                ?: return Result.failure()

            repository.refreshResults(matchData)

            if (matchData.selected) {


                if (matchData.getLatestMap().LogID != 0) {
                    val latestMap = matchData.getLatestMap()
                    if (latestMap.updated || latestMap.mapEnded) {

//                    matchData.oldLength = matchData.length

                        NotificationHelper.showNotification(
                            context = applicationContext,
                            title = "${matchData.match.clan1.name} vs ${matchData.match.clan2.name}",

                            message = "${
                                if (latestMap.mapEnded) {
                                    "Final Score after"
                                } else {
                                    "Minute"
                                }
                            }  ${(latestMap.length / 60)}:" +
                                    "${(latestMap.length % 60)}  ${latestMap.blue_score} - ${latestMap.red_score} on ${latestMap.name}",
                            notificationId = matchId
                        )

                    }

                }

                matchData.match.time = (System.currentTimeMillis() / 1000) + 30
                storageRepository.updateMatch(matchData)

                if (!matchData.matchEnded) {
                    repository.scheduleMatch(applicationContext, matchData.match)
                }
            }

            Result.success()

        } catch (e: Exception) {

            e.printStackTrace()

            Result.retry()
        }
    }

}