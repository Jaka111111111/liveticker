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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MatchScreen(mvm: MainViewModel = viewModel<MainViewModel>()) {
    val matchViewModels = mvm.matchData
    val loading = mvm.loading
    val error = mvm.error
    val context = LocalContext.current
    var playerIDs by remember { mutableStateOf("") }
    var filterBySelected by remember { mutableStateOf(false) }
    var filterByString by remember { mutableStateOf("") }

    LaunchedEffect(context) {
        mvm.getLocalMatches(context)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xff131a1f))
    ) {


        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                onClick = {
                    filterBySelected = false
                        mvm.getMatchesFromEtf2l(context)
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff272f36))
            ) {
                MediumTextWhite("Upcoming Matches")
            }

            Button(
                onClick = {
                    filterBySelected = true
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff272f36))
            ) {
                MediumTextWhite("Marked Matches")
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
        Row(verticalAlignment = Alignment.Top) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                value = filterByString,
                singleLine = true,
                onValueChange = { filterByString = it },
                label = { SmallTextWhite("Search for team, division or competition names") },
                textStyle = TextStyle(color = White, fontSize = 15.sp),
                shape = RoundedCornerShape(12.dp)
            )
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
            items(matchViewModels.sortedBy { matchData -> matchData.match.time }) { matchData ->

                if (((!filterBySelected && matchData.match.time * 1000 + 20000 > System.currentTimeMillis())
                            || (filterBySelected && matchData.observableSelected)) && matchData.containsFilter(
                        filterByString
                    )
                ) {
                    MatchOverview(
                        matchData = matchData, mvm
                    )
                }
            }
            item {
                if (!filterBySelected && matchViewModels.isNotEmpty()) {
                    Button(
                        onClick = {
                            mvm.nextPageToLoad++
                            mvm.getMatchesFromEtf2l(context, page = mvm.nextPageToLoad)
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff272f36))
                    ) {
                        MediumTextWhite("Load page ${mvm.nextPageToLoad + 1}")
                    }
                }
            }
        }

    }
}
@Composable
fun MatchTitleText(text: String) {
    Text(
        text = text,
        color = Color(0xffe68624),
        textAlign = TextAlign.Center,
        fontSize = 25.sp
    )
}
@Composable
fun MediumTextWhite(text: String) {
    Text(
        text = text,
        color = White,
        textAlign = TextAlign.Center,
        fontSize = 17.sp
    )
}
@Composable
fun SmallTextWhite(text: String) {
    Text(
        text = text,
        color = White,
        textAlign = TextAlign.Center,
        fontSize = 15.sp
    )
}
@Composable
fun MatchOverview(matchData: MatchData, mainViewModel: MainViewModel) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(12))
            .background(Color(0xff364049)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        MatchTitleText("${matchData.match.clan1.name} vs ${matchData.match.clan2.name}")

        MediumTextWhite(matchData.match.competition.name)
        MediumTextWhite(matchData.getDateStringFormatted())

        if (matchData.matchEnded) {
            MediumTextWhite(matchData.matchInfoStringBuilder())

        }
        Button(
            onClick = {
                matchData.observableSelected = !matchData.observableSelected
                mainViewModel.selectMatch(
                    context,
                    MatchData(matchData.match),
                    selected = matchData.observableSelected
                )
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff272f36))
        ) {
            if (matchData.observableSelected) {
                MediumTextWhite("Dont care")//, color = Color(0xffe68624))
            } else {
                MediumTextWhite("Notify me")//, color = Color(0xffe68624))
            }
        }
    }
}
