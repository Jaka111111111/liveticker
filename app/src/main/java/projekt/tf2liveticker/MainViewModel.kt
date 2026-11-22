package projekt.tf2liveticker

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import projekt.tf2liveticker.helper_classes.NotificationHelper
import kotlin.collections.List

class MainViewModel : ViewModel() {

    private val repository = MatchRepository()

    var matchDataList by mutableStateOf<List<MatchData>>(emptyList())
        private set

    var filteredMatchData by mutableStateOf<List<MatchData>>(matchDataList)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var filterBySelected by mutableStateOf(false)
    var filterByString by mutableStateOf("")
    var lastUpdated by mutableLongStateOf(0)
    var nextPageToLoad = 1

    fun applyFilter() {
        var helpList = mutableListOf<MatchData>()
        matchDataList.forEach {
            if (((!filterBySelected && it.match.time * 1000 + MinuteToMS.MINUTES_30 >= System.currentTimeMillis()) ||
                    (filterBySelected && it.observableSelected)) && it.containsFilter(filterByString))
            helpList.add(it)
        }
        filteredMatchData = helpList
    }

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
                val md = MatchData(m, mapsNeededToWin = 2, bestOf = 3, mapTiesAllowed = false)
                val players = playerIDs.split(",")
                md.team1data.players = mutableListOf<PlayerLog>(
                    PlayerLog(
                        "",
                        0,
                        "",
                        "",
                        PlayerSteam("", "", "", players[0]),
                        ""
                    )
                )
                md.team2data.players = mutableListOf<PlayerLog>(
                    PlayerLog(
                        "",
                        0,
                        "",
                        "",
                        PlayerSteam("", "", "", players[1]),
                        ""
                    )
                )
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
                storageRepository.selectMatch(matchData)

                if (matchData.selected) {
                    repository.scheduleMatch(context, matchData.match)
                }

            } catch (e: Exception) {
                error = e.message ?: "Unbekannter Fehler"
                //debug slop
                NotificationHelper.showDebugNotification(
                    context = context,
                    title = "Fehler",
                    message = "$error",
                    notificationId = 2
                )
            } finally {
                loading = false
            }
        }
    }

    fun getAppStats(context: Context) {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                repository.getAppStats(context)

                lastUpdated = repository.appSettings.lastUpdated

            } catch (e: Exception) {

                error = e.message ?: "Unbekannter Fehler"

            } finally {

                loading = false
            }
        }
    }

    fun saveAppStats(context: Context) {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                repository.saveAppStats(context, AppSettings(lastUpdated))

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
                if (repository.matchDataList.isEmpty()) {
                    repository.getLocalMatches(context)
                }
                matchDataList = repository.matchDataList

                for (md in matchDataList) {
                    md.observableSelected = md.selected
                }


            } catch (e: Exception) {

                error = e.message ?: "Unbekannter Fehler"

            } finally {
                applyFilter()
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
                lastUpdated = System.currentTimeMillis()
                saveAppStats(context)
                getLocalMatches(context)

            } catch (e: Exception) {
                error = e.message ?: "Unbekannter Fehler"
                getLocalMatches(context)
            } finally {
                loading = false
            }
        }


    }
}
