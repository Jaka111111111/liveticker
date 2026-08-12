package projekt.tf2liveticker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MatchScreen(
    mvm: MainViewModel = viewModel()
) {
    val matches by mvm::matches
    val loading by mvm::loading
    val error by mvm::error

    Row(verticalAlignment = Alignment.Top) {

        Button(
            onClick = {
                mvm.getMatches()
            }
        ) {
            Text("Refresh")
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
    Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
    ) {
        matches.forEach {
            MatchOverview(it, mvm)
        }
    }
}


@Composable
fun MatchOverview(match: Match, mvm: MainViewModel)
{

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp).background(color = Color.Blue)
    ) {

        Text(
            text = match.competition.name
        )

        Text(
            text = "${match.clan1.name} vs ${match.clan2.name}"
        )

        Text(
            text = "${
                java.time.format.DateTimeFormatter.ISO_INSTANT
                    .format(java.time.Instant.ofEpochSecond(match.time))
            }"
        )

        Button(
            onClick = {
                mvm.toggleMatchSelect(match.id)
            }
        ) {
            Text("Notify me")
        }
    }
}