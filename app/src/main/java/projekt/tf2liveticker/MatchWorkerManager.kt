package projekt.tf2liveticker

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import okhttp3.internal.platform.PlatformRegistry.applicationContext
import java.util.concurrent.TimeUnit

class MatchWorkerManager() {
    val matchesActive: Boolean = false
    private val repository = MatchRepository()
    private val storageRepository =
        MatchStorageRepository(applicationContext!!)

    suspend fun runWorkers() {
        var matchesToUpdate = storageRepository.getActiveMatches()
        while (matchesToUpdate.isNotEmpty()) {
            matchesToUpdate.forEach {

                val inputData = workDataOf(
                    "MATCH_ID" to it.match.id
                )

                val request = OneTimeWorkRequestBuilder<MatchUpdateWorker>()
                    .setInitialDelay(0, TimeUnit.MILLISECONDS) //DELAY
                    .setInputData(inputData)
                    .addTag("LiveTicker")
                    .build()

                WorkManager
                    .getInstance(applicationContext!!)
                    .enqueueUniqueWork("Match_" + it.match.id, ExistingWorkPolicy.REPLACE, request)
            }

            withContext(Dispatchers.IO) {
                Thread.sleep(MinuteToMS.MINUTE_01)
            }

            matchesToUpdate = storageRepository.getActiveMatches()
        }
    }

}