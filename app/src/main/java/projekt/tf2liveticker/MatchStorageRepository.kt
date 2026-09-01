package projekt.tf2liveticker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import projekt.tf2liveticker.Models.MatchData
import projekt.tf2liveticker.Models.MatchDataList

class MatchStorageRepository(
    private val context: Context
) {
    val matches: Flow<List<MatchData>> =
        context.dataStore.data.map { matchDataList ->
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

    suspend fun getMatch(matchId: Int): MatchData? {

        val d = context.matchDataStore.data.first()
        val data = context.dataStore.data.first()

        return d.MatchData.find {
            it.match.id == matchId
        }
    }

    suspend fun updateMatch(updatedMatch: MatchData) {

        context.dataStore.updateData { currentData ->

            currentData.copy(
                MatchData = currentData.MatchData.map {

                    if (it.match.id == updatedMatch.match.id) {
                        updatedMatch
                    } else {
                        it
                    }
                }
            )
        }
    }
}

val Context.matchDataStore: DataStore<MatchDataList> by dataStore(
    fileName = "matches.json",
    serializer = MatchDataSerializer
)