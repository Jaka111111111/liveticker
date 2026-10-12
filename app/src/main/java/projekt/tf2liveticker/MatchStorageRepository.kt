package projekt.tf2liveticker

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import projekt.tf2liveticker.Models.Clan
import projekt.tf2liveticker.Models.Competition
import projekt.tf2liveticker.Models.Division
import projekt.tf2liveticker.Models.Match
import projekt.tf2liveticker.Models.MatchData
import projekt.tf2liveticker.Models.MatchDataList

class MatchStorageRepository(
    private val context: Context
) {
    val matches: Flow<List<MatchData>> =
        context.matchDataStore.data.map { matchDataList ->
            matchDataList.MatchData
        }

    suspend fun saveMatch(matchData: MatchData) {

        context.matchDataStore.updateData { currentData ->

            if (currentData.MatchData.any { it.match.id == matchData.match.id }) {
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
        val m = Match(92991, c1, c2, com, d, ms, "1", System.currentTimeMillis() / 1000)
        val nationsCupdummy = MatchData(m)
        saveMatch(nationsCupdummy)
    }

    suspend fun getMatch(matchId: Int): MatchData? {

        val d = context.matchDataStore.data.first()

        return d.MatchData.find {
            it.match.id == matchId
        }
    }

    suspend fun getAllMatches(): List<MatchData> {
        val d = context.matchDataStore.data.first()
        return d.MatchData
    }

    suspend fun updateMatch(updatedMatch: MatchData) {
        Log.d("STORAGE", "updateMatch gestartet")
        Log.d("STORAGE", "Match ID: ${updatedMatch.match.id}")

        context.matchDataStore.updateData { currentData ->

            Log.d(
                "STORAGE",
                "Aktuelle Matches: ${currentData.MatchData.size}"
            )

            val updatedList = currentData.MatchData.map { storedMatch ->

                if (storedMatch.match.id == updatedMatch.match.id) {
                    updatedMatch
                } else {
                    storedMatch
                }
            }

            Log.d(
                "STORAGE",
                "Neue Matches: ${updatedList.size}"
            )

            currentData.copy(
                MatchData = updatedList
            )
        }

        Log.d("STORAGE", "updateMatch beendet")
//        context.matchDataStore.updateData { currentData ->
//
//            val exists = currentData.MatchData.any {
//                it.match.id == updatedMatch.match.id
//            }
//
//            if (exists) {
//
//                currentData.copy(
//                    MatchData = currentData.MatchData.map {
//                        if (it.match.id == updatedMatch.match.id) {
//                            updatedMatch
//                        } else {
//                            it
//                        }
//                    }
//                )
//
//            } else {
//
//                currentData.copy(
//                    MatchData = currentData.MatchData + updatedMatch
//                )
//            }
//        }
//    }
    }

}

val Context.matchDataStore: DataStore<MatchDataList> by dataStore(
    fileName = "matches.json",
    serializer = MatchDataSerializer
)