package projekt.tf2liveticker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
@Composable
fun MatchScreen(
    mvm: MainViewModel = viewModel()
) {
    val matchViewModels = mvm.matchViewModels
    val loading = mvm.loading
    val error = mvm.error

    var filter by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {


        Row(verticalAlignment = Alignment.Top) {

            Button(
                onClick = {
                    filter = 0
                    mvm.getMatches()
                }
            ) {
                Text("Upcoming Matches")
            }

            Button(
                onClick = {
                    filter = 1
                }
            ) {
                Text("Marked Matches")
            }

            if (loading) {

                CircularProgressIndicator()

            }

            error?.let {

                Text(
                    text = "Fehler: $it"
                )
            }

        }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            items(matchViewModels) { matchVM ->

                if (filter == 0 || matchVM.selected) {
                    MatchOverview(
                        matchvm = matchVM
                    )
                }
            }

        }
    }
}
    @Composable
    fun MatchOverview(matchvm: MatchViewModel) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp).background(color = Color.Blue)
        ) {

            Text(
                text = matchvm.match.competition.name
            )

            Text(
                text = "${matchvm.match.clan1.name} vs ${matchvm.match.clan2.name}"
            )

            Text(
                text = "${
                    java.time.format.DateTimeFormatter.ISO_INSTANT
                        .format(java.time.Instant.ofEpochSecond(matchvm.match.time))
                }"
            )

            Button(
                onClick = {
                    matchvm.selected = !matchvm.selected
                }
            ) {
                if (matchvm.selected) {
                    Text("Dont care")
                } else {
                    Text("Notify me")
                }
            }
        }
    }

