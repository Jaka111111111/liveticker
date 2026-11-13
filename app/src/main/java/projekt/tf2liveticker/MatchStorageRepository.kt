package projekt.tf2liveticker

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.selects.select

class MatchStorageRepository(
    private val context: Context
) {
    val matchDataList: Flow<List<MatchData>> =
        context.matchDataStore.data.map { matchDataList ->
            matchDataList.MatchData
        }

    suspend fun saveMatch(matchData: MatchData) {
        context.matchDataStore.updateData { currentData ->

//            for (oldMatchData in currentData.MatchData) {
//                if (matchData.match.id == oldMatchData.match.id) {
//                    oldMatchData.match.time = matchData.match.time
//                }
//            } //???
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

    suspend fun getAllMatches(): List<MatchData> {
        val d = context.matchDataStore.data.first()
        return d.MatchData
    }

    suspend fun updateMatch(updatedMatch: MatchData) {

        context.matchDataStore.updateData { currentData ->

            val updatedList = currentData.MatchData.map { storedMatch ->

                if (storedMatch.match.id == updatedMatch.match.id) {
                    updatedMatch
                } else {
                    storedMatch
                }
            }

            currentData.copy(
                MatchData = updatedList
            )
        }

    }

}

val Context.matchDataStore: DataStore<MatchDataList> by dataStore(
    fileName = "matches.json",
    serializer = MatchDataSerializer
)