package projekt.tf2liveticker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import dev.chrisbanes.accompanist.coil.CoilImage
import projekt.tf2liveticker.Models.MatchData
import projekt.tf2liveticker.ViewModels.MainViewModel

@Composable
fun MatchScreen(mvm: MainViewModel = viewModel<MainViewModel>())
{
    val matchViewModels = mvm.matchData
    val loading = mvm.loading
    val error = mvm.error
    val context = LocalContext.current
    var dummylog by remember { mutableStateOf("") }

    var filter by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {


        Row(verticalAlignment = Alignment.Top) {

            Button(
                onClick = {
                    filter = 0
                    mvm.getMatches(context)
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
        Row(verticalAlignment = Alignment.Top) {
            OutlinedTextField(
                value = dummylog,
                onValueChange = { dummylog = it },
                label = { Text("...") },
                shape = RoundedCornerShape(12.dp)
            )
            Button(onClick = {mvm.refreshResultsDummy(context, dummylog.toInt())}) {
                Text("Find")
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(matchViewModels) { matchVM ->

                if (filter == 0 || matchVM.observableSelected) {
                    MatchOverview(
                        matchvm = matchVM, mvm
                    )
                }
            }

        }
    }
}

@Composable
fun TeamAvatar(data: String) {
    CoilImage(
        data = data,
        contentDescription = "android",
        alignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.60f),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier.background(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                )
            )
        },
        error = {
            Box(
                modifier = Modifier.background(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                )
            )
        }
    )
}

@Composable
fun MatchOverview(matchvm: MatchData, mvm: MainViewModel)
{

    val context = LocalContext.current

    Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp).background(color = androidx.compose.ui.graphics.Color(0xFF6750A4))
        )
    {
        Text(
            text = matchvm.match.competition.name, color = White
        )

        Text(
            text = "${matchvm.match.clan1.name} vs ${matchvm.match.clan2.name}", color = White
        )

        Text(
            text = "${
                java.time.format.DateTimeFormatter.ISO_INSTANT
                    .format(java.time.Instant.ofEpochSecond(matchvm.match.time))
            }", color = White
        )
        Button(
            onClick = {
                //matchvm.selected = !matchvm.selected
                //mvm.getParticipants(matchvm)
                mvm.selectMatch(
                    context,
                    matchvm.match,
                    selected = !matchvm.observableSelected)
            }
        ) {
            if (matchvm.observableSelected) {
                Text("Dont care")
            } else {
                Text("Notify me")
            }
        }
    }
}
