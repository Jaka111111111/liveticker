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

            val matchData = storageRepository.getMatch(matchId)
                ?: return Result.failure()


            if (matchData.selected) {

                repository.refreshResults(matchData)

                if (matchData.getLatestMap().logID != 0) {
                    val latestMap = matchData.getLatestMap()
                    if (latestMap.updated || latestMap.mapEnded) {


                        NotificationHelper.showNotification(
                            context = applicationContext,
                            title = "${matchData.match.clan1.name} vs ${matchData.match.clan2.name}",

                            message = "${latestMap.blueScore} - ${latestMap.redScore} on map ${matchData.mutableMapDataList.size} ${latestMap.name} \n"
                            +
                                    "${
                                if (latestMap.mapEnded) {
                                    "Final Score after"
                                } else {
                                    "Minute"
                                }
                            }  ${(latestMap.length / 60)}:${(latestMap.length % 60)}",
                            notificationId = latestMap.logID
                        )

                    }

                }

                //matchData.match.time = (System.currentTimeMillis() / 1000) + 30
                storageRepository.updateMatch(matchData)

                if (matchData.matchEnded) {
                    NotificationHelper.showNotification(
                        context = applicationContext,
                        title = "${matchData.match.clan1.name} vs ${matchData.match.clan2.name} concluded!",

                        message = "${matchData.team1score} - ${matchData.team2score} \n" +
                                matchData.mapInfoStringBuilder()
                                ,
                        notificationId = matchId
                    )
                }
                else {
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