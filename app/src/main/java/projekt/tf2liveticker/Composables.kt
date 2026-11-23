package projekt.tf2liveticker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import projekt.tf2liveticker.helper_classes.CalendarHelper
import projekt.tf2liveticker.helper_classes.ComposableColors

@Composable
fun ProgressIndicator() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            color = ComposableColors.highlightText,
            modifier = Modifier
                .align(Alignment.Center)
                .zIndex(10f)
        )
    }
}

@Composable
fun MatchScreen(mvm: MainViewModel) {
    val matchViewModels = mvm.filteredMatchData
    val loading = mvm.loading
    val error = mvm.error
    val context = LocalContext.current
    var playerIDs by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposableColors.backdrop)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    mvm.filterBySelected = false
                    mvm.applyFilter()
                },
                colors = ButtonDefaults.buttonColors(ComposableColors.getButtonColor(!mvm.filterBySelected))
            ) {
                MediumTextWhite("Upcoming Matches")
            }

            Button(
                onClick = {
                    mvm.filterBySelected = true
                    mvm.filterByString = ""
                    mvm.applyFilter()
                },
                colors = ButtonDefaults.buttonColors(ComposableColors.getButtonColor(mvm.filterBySelected))
            ) {
                MediumTextWhite("Marked Matches")
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
                value = mvm.filterByString,
                singleLine = true,
                onValueChange = { mvm.filterByString = it; mvm.applyFilter() },
                label = { SmallText("Search for team, division or competition names") },
                textStyle = TextStyle(color = ComposableColors.regularText, fontSize = 15.sp),
                shape = RoundedCornerShape(12.dp)
            )
        }
        Row(verticalAlignment = Alignment.Top) {
            OutlinedTextField(
                value = playerIDs,
                onValueChange = { playerIDs = it },
                label = { Text("...") },
                textStyle = TextStyle(color = ComposableColors.regularText),
                shape = RoundedCornerShape(12.dp)
            )
            Button(onClick = { mvm.refreshResultsDummy(context, playerIDs) }) {
                Text("Find")
            }
        }

        SmallCursiveText("last updated: " + CalendarHelper().buildRelativeDayString(mvm.lastUpdated / 1000))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(matchViewModels.sortedBy { matchData -> matchData.match.time }) { matchData ->
                MatchOverview(
                    matchData = matchData, mvm
                )
            }
            item {
                if (matchViewModels.isEmpty() && !loading) {
                    SmallCursiveText("No local matches found. Try loading the next page")
                }
            }
            item {
                if (!mvm.filterBySelected && !loading) {
                    Button(
                        onClick = {
                            mvm.nextPageToLoad++
                            mvm.getMatchesFromEtf2l(context, page = mvm.nextPageToLoad)
                        }, colors = ButtonDefaults.buttonColors(ComposableColors.buttonUnclicked)
                    ) {
                        MediumTextWhite("Load page ${mvm.nextPageToLoad + 1}")
                    }
                }
            }
        }

        if (loading) {
            ProgressIndicator()
        }

    }
}

@Composable
fun BigTitleText(text: String) {
    Text(
        text = text,
        color = ComposableColors.highlightText,
        textAlign = TextAlign.Center,
        fontSize = 25.sp,
        modifier = Modifier.padding(all = 5.dp)
    )
}

@Composable
fun MediumTextWhite(text: String) {
    Text(
        text = text,
        color = ComposableColors.regularText,
        textAlign = TextAlign.Center,
        fontSize = 17.sp
    )
}

@Composable
fun SmallText(text: String) {
    Text(
        text = text,
        color = ComposableColors.regularText,
        textAlign = TextAlign.Center,
        fontSize = 15.sp,
        maxLines = 1
    )
}

@Composable
fun SmallCursiveText(text: String) {
    Text(
        text = text,
        color = ComposableColors.regularText,
        textAlign = TextAlign.Center,
        fontSize = 15.sp,
        fontStyle = FontStyle.Italic,
        maxLines = 1,
        modifier = Modifier.padding(all = 5.dp)
    )
}

@Composable
fun MatchOverview(matchData: MatchData, mainViewModel: MainViewModel) {

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12))
            .background(ComposableColors.itemFill)
            .clickable(
                enabled = true,
                onClick = { expanded = !expanded }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        BigTitleText("${matchData.match.clan1.name} vs ${matchData.match.clan2.name}")

        if (matchData.match.time * 1000 < System.currentTimeMillis() && matchData.selected) {
            BigTitleText("${matchData.team1score} - ${matchData.team2score}")
        }

        MediumTextWhite(matchData.match.competition.name)

        MediumTextWhite(matchData.match.division.name)
        MediumTextWhite(matchData.getDateStringFormatted())

        if (matchData.matchStarted()) {
            MediumTextWhite(matchData.matchInfoStringBuilder())
        }

        if (expanded) {
            matchData.match.maps.forEach {
                MediumTextWhite(it)
            }
            MediumTextWhite("Best of " + matchData.bestOf.toString())
            MediumTextWhite(
                if (matchData.mapTiesAllowed) {
                    "Ties are possible"
                } else {
                    "Ties are not possible"
                }
            )
        }

        Button(
            onClick = {
                matchData.observableSelected = !matchData.observableSelected
                mainViewModel.selectMatch(
                    context,
                    matchData,
                    selected = matchData.observableSelected,
                )
            },
            colors = ButtonDefaults.buttonColors(ComposableColors.getButtonColor(matchData.observableSelected)),
            modifier = Modifier.padding(5.dp)
        ) {
            if (matchData.observableSelected) {
                MediumTextWhite("Dont care")
            } else {
                MediumTextWhite("Notify me")
            }
        }
    }

}
