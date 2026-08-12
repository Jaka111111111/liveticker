package projekt.tf2liveticker

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel: ViewModel()
{
    val selectedMatches = mutableListOf<Int>()

    var matches by mutableStateOf<List<Match>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun toggleMatchSelect(id: Int) {
        var found = false
        selectedMatches.forEach {
            if (it == id) {
                selectedMatches.remove(it)
                found = true
            }
        }
        if (!found) { selectedMatches.add(id) }
    }

    fun getMatches() {
        viewModelScope.launch {
            loading = true
            error = null

            try {

                val response = RetrofitInstance.api.getMatches()

                matches = response.results.data


            } catch (e: Exception) {

                error = e.message ?: "Unbekannter Fehler"

            } finally {

                loading = false
            }
        }
    }
}