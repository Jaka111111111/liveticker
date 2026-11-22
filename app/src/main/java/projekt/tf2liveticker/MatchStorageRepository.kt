package projekt.tf2liveticker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import projekt.tf2liveticker.helper_classes.NotificationHelper

class MatchStorageRepository(
    private val context: Context
) {

    suspend fun saveMatch(matchData: MatchData) {
        context.matchDataStore.updateData { currentData ->
            if (currentData.MatchData.any { it.match.id == matchData.match.id}) {
                currentData
            } else {
                currentData.copy(
                    MatchData = currentData.MatchData + matchData
                )
            }
        }
    }



    suspend fun removeMatch(matchId: Int) {

        context.matchDataStore.updateData { currentData ->

            currentData.copy(
                MatchData = currentData.MatchData.filter {
                    it.match.id != matchId
                }
            )
        }
    }

    suspend fun removeAllMatches() {
        context.matchDataStore.updateData { _ ->
            MatchDataList()
        }
    }

    suspend fun addDummyData() {
        val c1 = Clan("USA", 15474, "USA")
        val c2 = Clan("Europe", 7776, "England")
        val com = Competition("quatsch", 2, "Nations Cup #11 Grand Finals", "6v6")
        val d = Division(9, "hi", 1, 1)
        val ms = emptyList<String>()
        val m = Match(92991, c1, c2, com, d, ms, "1", 1788431910)
        val nationsCupdummy = MatchData(m)
        nationsCupdummy.bestOf = 1
        saveMatch(nationsCupdummy)
    }

    suspend fun getMatch(matchId: Int): MatchData? {

        val d = context.matchDataStore.data.first()

        return d.MatchData.find {
            it.match.id == matchId
        }
    }

    suspend fun getAppStats(): AppSettings {
        val d = context.appStatStore.data.first()
        return d
    }

    suspend fun updateAppStats(appSettings: AppSettings) {
        context.appStatStore.updateData { appSettings }
    }

    suspend fun getAllMatches(): List<MatchData> {
        val d = context.matchDataStore.data.first()
        return d.MatchData
    }

    suspend fun getActiveMatches(): ArrayList<MatchData> {
        val d = context.matchDataStore.data.first()
        var activeMatches = ArrayList<MatchData>()
        d.MatchData.forEach { currentData ->
            if (
                   currentData.selected
                &&!currentData.matchEnded)
            {
                if (currentData.matchStarted()) {
                    activeMatches.add(currentData)
                }
            }
        }
        return activeMatches
    }

    suspend fun selectMatch (matchData: MatchData) {
        context.matchDataStore.updateData { currentData ->

            val updatedList = currentData.MatchData.map { storedMatch ->

                if (storedMatch.match.id == matchData.match.id) {
                    matchData
                } else {
                    storedMatch
                }
            }

            currentData.copy(
                MatchData = updatedList
            )
        }
    }
    suspend fun updateMatch(updatedMatch: MatchData): Boolean {
        var changed = false
        context.matchDataStore.updateData { currentData ->
            val updatedList = currentData.MatchData.map { storedMatch ->

                if (storedMatch.match.id == updatedMatch.match.id)
                {
                    if (storedMatch.match.time != updatedMatch.match.time) {
                        changed = true
                    }
                    updatedMatch.selected = storedMatch.selected
                    updatedMatch
                } else {
                    storedMatch
                }
            }

            currentData.copy(
                MatchData = updatedList
            )
        }
        return changed
    }

    suspend fun updateMatches(matches: List<Match>) {
        var matchUpdatedCounter = 0
        matches.forEach {
            if (getMatch(it.id) != null) {
                val changed = updateMatch(MatchData(match = it))
                if (changed) {
                    matchUpdatedCounter++
                }
            }
            else {
                saveMatch(MatchData(match = it))
                matchUpdatedCounter++
            }
        }

        //debug slop
        NotificationHelper.showDebugNotification(
            context = context,
            title = "$matchUpdatedCounter Matches wurden aktualisiert",
            message = "hi",
            notificationId = 1
        )
    }
}
val Context.appStatStore: DataStore<AppSettings> by dataStore(
    fileName = "stats.json",
    serializer = AppStatsSerializer
)
val Context.matchDataStore: DataStore<MatchDataList> by dataStore(
    fileName = "matches.json",
    serializer = MatchDataSerializer
)