package projekt.tf2liveticker

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class MatchViewModel(
    val match: Match
) {
    var selected by mutableStateOf(false)
}
class MainViewModel: ViewModel()
{
    var matchViewModels by mutableStateOf<List<MatchViewModel>>(emptyList())
        private set

    var matches by mutableStateOf<List<Match>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set


    fun getMatches() {
        viewModelScope.launch {
            loading = true
            error = null

            try {
//
//                val response = RetrofitInstance.api.getMatches()
//
//                matches = response.results.data
//
//                matchViewModels = response.results.data.map{
//                    MatchViewModel(it)
//                }
                val response = RetrofitInstance.api.getMatches()

                matches = response.results.data

                val oldViewModels = matchViewModels.associateBy { it.match.id }

                matchViewModels = response.results.data.map { match ->

                    oldViewModels[match.id]
                        ?: MatchViewModel(match)
                }

            } catch (e: Exception) {

                error = e.message ?: "Unbekannter Fehler"

            } finally {

                loading = false
            }
        }
    }
}