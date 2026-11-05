package projekt.tf2liveticker

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import dev.chrisbanes.accompanist.coil.CoilImage
import java.util.Date

@Composable
fun MatchScreen(mvm: MainViewModel = viewModel<MainViewModel>())
{
    val matchViewModels = mvm.matchData
    val loading = mvm.loading
    val error = mvm.error
    val context = LocalContext.current
    var playerIDs by remember { mutableStateOf("") }
    var filterBySelected by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().background(color = Color(0xff3108BC))
    ) {


        Row(verticalAlignment = Alignment.Top) {

            Button(
                onClick = {
                    filterBySelected = false
                    if (mvm.matchData.isEmpty()) {
                        mvm.getMatchesFromEtf2l(context)
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff5D30F6))
            ) {
                Text("Upcoming Matches")
            }

            Button(
                onClick = {
                    filterBySelected = true
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff5D30F6))
            ) {
                Text("Marked Matches")
            }

            if (loading) {

                CircularProgressIndicator(color = Color(0xff5D30F6))

            }

            error?.let {

                Text(
                    text = "Fehler: $it"
                )
            }

        }
//        Row(verticalAlignment = Alignment.Top) {
//            OutlinedTextField(
//                value = playerIDs,
//                onValueChange = { playerIDs = it },
//                label = { Text("...") },
//                shape = RoundedCornerShape(12.dp)
//            )
//            Button(onClick = {mvm.refreshResultsDummy(context, playerIDs)}) {
//                Text("Find")
//            }
//        }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(matchViewModels.sortedBy { matchData -> matchData.match.time }) { matchVM ->

                if ((!filterBySelected && matchVM.match.time*1000 + 20000 > System.currentTimeMillis())
                    || (filterBySelected && matchVM.observableSelected)) {
                    MatchOverview(
                        matchData = matchVM, mvm
                    )
                }
            }
            item {
                if (!filterBySelected) {
                    Button(
                        onClick = {
                            mvm.nextPageToLoad++
                            mvm.getMatchesFromEtf2l(context, page = mvm.nextPageToLoad)
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff5D30F6))
                    ) {
                        Text("Load page ${mvm.nextPageToLoad + 1}")
                    }
                }
            }
        }

    }
}

@Composable
fun MatchOverview(matchData: MatchData, mainViewModel: MainViewModel)
{

    val context = LocalContext.current

    Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(Color(0xff3D0AEB))
        )
    {
        Text(
            text = "${matchData.match.clan1.name} vs ${matchData.match.clan2.name}", color = White, textAlign = TextAlign.Center
        )

        Text(
            text = matchData.match.competition.name, color = White
        )

        Text(
            text = matchData.getDateStringFormatted(), color = White
        )
        Button(
            onClick = {
                matchData.observableSelected = !matchData.observableSelected
                mainViewModel.selectMatch(
                    context,
                    MatchData(matchData.match),
                    selected = matchData.observableSelected)
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff5D30F6))
        ) {
            if (matchData.observableSelected) {
                Text("Dont care")
            } else {
                Text("Notify me")
            }
        }
    }
}
