package projekt.tf2liveticker

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = MatchRepository()

    var matchData by mutableStateOf<List<MatchData>>(emptyList())
        private set

    var matches by mutableStateOf<List<Match>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var nextPageToLoad = 1

    fun getParticipants(matchData: MatchData) {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                repository.getParticipants(matchData)

            } catch (e: Exception) {

                error = e.message ?: "Unbekannter Fehler"

            } finally {

                loading = false
            }
        }
    }

    fun refreshResultsDummy(context: Context, playerIDs: String) {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                val c1 = Clan("England", 0, "Placeholder")
                val c2 = Clan("England", 1, "Holdplacer")
                val com = Competition("Placeholder", 2, "Placeholder", "6v6")
                val d = Division(9, "hi", 1, 1)
                val ms = emptyList<String>()
                val m = Match(0, c1, c2, com, d, ms, "1", System.currentTimeMillis() / 1000)
                val md = MatchData(m)
                val players = playerIDs.split(",")
                md.team1.players = mutableListOf<PlayerLog>(PlayerLog("", 0, "", "", PlayerSteam("", "", "", players[0]), ""))
                md.team2.players = mutableListOf<PlayerLog>(PlayerLog("", 0, "", "", PlayerSteam("", "", "", players[1]), ""))
                selectMatch(context, md, 0)

                repository.refreshResultsDummy(context, playerIDs)

            } catch (e: Exception) {
                error = e.message ?: "Unbekannter Fehler"
            } finally {
                loading = false
            }
        }
    }

    fun selectMatch(
        context: Context,
        matchData: MatchData,
        dummylivelog: Int = 0,
        selected: Boolean = true
    ) {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                val storageRepository =
                    MatchStorageRepository(context)

                matchData.selected = selected
                matchData.observableSelected = selected
                matchData.getLatestMap().logID = dummylivelog
                storageRepository.updateMatch(matchData)

                if (matchData.selected) {
                    repository.scheduleMatch(context, matchData.match)
                }

            } catch (e: Exception) {
                error = e.message ?: "Unbekannter Fehler"
            } finally {
                loading = false
            }
        }
    }

    fun getLocalMatches(context: Context) {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                repository.getLocalMatches(context)

                matches = repository.matches

                matchData = repository.matchData

                for (md in matchData) {
                    md.observableSelected = md.selected
                }


            } catch (e: Exception) {

                error = e.message ?: "Unbekannter Fehler"

            } finally {

                loading = false
            }
        }
    }

    fun getMatchesFromEtf2l(context: Context, page: Int = 1) {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                repository.getEtf2lMatches(context, page)

                matches = repository.matches

                matchData = repository.matchData

                for (md in matchData) {
                    md.observableSelected = md.selected
                }


            } catch (e: Exception) {

                error = e.message ?: "Unbekannter Fehler"

            } finally {

                loading = false
            }
        }
    }
}