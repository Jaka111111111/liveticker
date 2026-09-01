package projekt.tf2liveticker.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import projekt.tf2liveticker.Models.Match
import projekt.tf2liveticker.Models.MatchData
import projekt.tf2liveticker.MatchRepository
import projekt.tf2liveticker.MatchStorageRepository
import projekt.tf2liveticker.Models.Clan
import projekt.tf2liveticker.Models.Competition
import projekt.tf2liveticker.Models.Division

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

    fun refreshResultsDummy(context: Context, logID: Int) {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                val c1 = Clan("England", 0, "Placeholder")
                val c2 = Clan("England", 1, "Holdplacer")
                val com = Competition("Placeholder", 2, "Placeholder", "6v6")
                val d = Division(9, "hi", 1, 1)
                val ms = emptyList<String>()
                val m = Match(logID, c1, c2, com, d, ms, "1", System.currentTimeMillis() / 1000)

                scheduleMatch(context, m, logID)

                repository.refreshResultsDummy(context, logID)

            } catch (e: Exception) {
                error = e.message ?: "Unbekannter Fehler"
            } finally {
                loading = false
            }
        }
    }

    fun scheduleMatch(
        context: Context,
        match: Match,
        dummylivelog: Int = 0
    ) {
        viewModelScope.launch {
            loading = true
            error = null

            try {
                val storageRepository =
                    MatchStorageRepository(context)

                val md = MatchData(
                    match = match,
                )
                md.livelog = dummylivelog
                md.selected = true

                storageRepository.saveMatch(
                    md
                )
                repository.scheduleMatch(context, match)

            } catch (e: Exception) {
                error = e.message ?: "Unbekannter Fehler"
            } finally {
                loading = false
            }
        }
    }

    fun getMatches() {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                repository.getMatches()

                matches = repository.matches

                matchData = repository.matchData


            } catch (e: Exception) {

                error = e.message ?: "Unbekannter Fehler"

            } finally {

                loading = false
            }
        }
    }
}