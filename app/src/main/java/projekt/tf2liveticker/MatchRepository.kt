package projekt.tf2liveticker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import projekt.tf2liveticker.Models.Clan
import projekt.tf2liveticker.Models.Competition
import projekt.tf2liveticker.Models.Division
import projekt.tf2liveticker.Models.MapData
import projekt.tf2liveticker.Models.Match
import projekt.tf2liveticker.Models.MatchData
import projekt.tf2liveticker.Models.Player
import projekt.tf2liveticker.Models.TeamData
import java.util.concurrent.TimeUnit

class MatchRepository() {

    var matchData: List<MatchData> = emptyList()
        private set

    var matches: List<Match> = emptyList()
        private set

    suspend fun findLiveLog(matchData: MatchData) {

        if (matchData.mutableMapDataList.last().LogID == 0) {

            if(matchData.team1.players.size == 0) {
                getParticipants(matchData)
            }
            Log.d("findLiveLog", "team1: ${matchData.team1.players.size}")
            Log.d("findLiveLog", "team2: ${matchData.team2.players.size}")

            matchData.team1.players.forEach { player ->

                val response = RetrofitLogInstance.log_api.getRecentLogs(player.steam?.id64)
                val latest = RetrofitLogInstance.log_api.getResults(response.logs.logs[0].id)
                var counter = 0
                val players = latest.players.map { (steam3Id, stats) ->

                    Player(
                        steam3Id = steam3Id,
                        name = latest.names[steam3Id] ?: "Unknown",
                        team = stats.team,
                        kills = stats.kills,
                        deaths = stats.deaths,
                        assists = stats.assists,
                        damage = stats.dmg
                    )
                }

                players.forEach { player ->
                    val id = player.steam3Id

                    matchData.team1.players.forEach {
                        if (it.steam?.id3 == id) {
                            if (player.team == "red") {
                                matchData.mutableMapDataList.last().red_team = matchData.team1.id
                                matchData.mutableMapDataList.last().blue_team = matchData.team2.id
                            }
                            counter++
                        }
                    }

                    matchData.team2.players.forEach {
                        if (it.steam?.id3 == id) {
                            if (player.team == "red") {
                                matchData.mutableMapDataList.last().red_team = matchData.team2.id
                                matchData.mutableMapDataList.last().blue_team = matchData.team1.id
                            }
                            counter++
                        }
                    }
                }

                if (counter >= 8) {
                    matchData.mutableMapDataList.last().LogID = response.logs.logs[0].id
                }
            }
        }
    }

    suspend fun refreshResultsDummy(context: Context, logId: Int) {
//        val response = RetrofitLogInstance.log_api.getResults(logId)
//        val red_score = response.teams["Red"]?.score!!
//        val blue_score = response.teams["Blue"]?.score!!
        val c1 = Clan("England", 0, "Placeholder")
        val c2 = Clan("England", 1, "Holdplacer")
        val com = Competition("Placeholder", 2, "Placeholder", "6v6")
        val d = Division(9, "hi", 1, 1)
        val ms = emptyList<String>()
        val m = Match(logId, c1, c2, com, d, ms, "1", System.currentTimeMillis() / 1000)
        val md = MatchData(m, bestOf = 1, team1 = TeamData(id = 0), team2 = TeamData(1))
        md.selected = true
        md.mutableMapDataList.add(MapData(LogID = logId))
        this.matchData += md
        val storageRepository =
            MatchStorageRepository(context)
        storageRepository.saveMatch(md)

        scheduleMatch(context, m)

    }

    suspend fun refreshResults(matchData: MatchData?) {
        if (matchData!!.selected) { //&& matchData.match.time * 1000L < System.currentTimeMillis()
            if (matchData.getLatestMap().LogID == 0) {
                findLiveLog(matchData)
            }
            if (matchData.mutableMapDataList.last().LogID != 0) {
                val response = RetrofitLogInstance.log_api.getResults(matchData.mutableMapDataList.last().LogID)
                var latestMap = matchData.getLatestMap()
                latestMap.updated = false
                if (latestMap.length != response.length) {
                    latestMap.updated = true
                    latestMap.lastUpdated = System.currentTimeMillis()

                    if (latestMap.red_score + latestMap.blue_score == response.teams["Red"]?.score!! + response.teams["Blue"]?.score!!) {
                        latestMap.mapEnded = true
                    }
                }

                if ((System.currentTimeMillis() - latestMap.lastUpdated) / 1000 > 1200) {
                    latestMap.mapEnded = true
                }

                latestMap.length = response.length
                latestMap.red_score = response.teams["Red"]?.score!!
                latestMap.blue_score = response.teams["Blue"]?.score!!

                if (latestMap.red_score == latestMap.blue_score) {
                    latestMap.winner = "tie"
                }
                else if (latestMap.red_score > latestMap.blue_score) {
                    latestMap.winner = "red"
                }
                else {
                    latestMap.winner = "blue"
                }

                if (latestMap.mapEnded) {
                    if (latestMap.winner.equals("red")) { //diggaaaaaaaaa
                        matchData.red_score++
                    }
                    else if (latestMap.winner.equals("blue")) {
                        matchData.blue_score++
                    }
                }

                if (latestMap.mapEnded && (matchData.red_score == matchData.mapsNeededToWin
                            || matchData.blue_score == matchData.mapsNeededToWin)) {
                    matchData.matchEnded = true
                    }
            }
        }
    }

    suspend fun getMatches(context: Context) {
        val storageRepository = MatchStorageRepository(context)
        val response = RetrofitEtf2lInstance.etf2l_api.getMatches()
        matches = response.results.data
        for (m in matches) {
            storageRepository.saveMatch(MatchData(m, team1 = TeamData(), team2 = TeamData()))
        }
        storageRepository.addDummyData()
        matchData = storageRepository.getAllMatches()
        matchData.first().observableSelected

    }

    suspend fun scheduleMatch(
        context: Context,
        match: Match
    ) {
        Log.e("WORKER", "1 - scheduleMatch gestartet")
        val matchTimeMillis = match.time * 1000L
        Log.e("WORKER", "2 - Zeit berechnet: $matchTimeMillis")
        val delay = matchTimeMillis - System.currentTimeMillis()
        Log.e("WORKER", "3 - Delay: $delay")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        Log.e("WORKER", "4 - Constraints erstellt")
        val inputData = workDataOf(
            "MATCH_ID" to match.id
        )
        Log.e("WORKER", "5 - InputData erstellt")
        val request = OneTimeWorkRequestBuilder<MatchUpdateWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS) //DELAY
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()
        Log.e("WORKER", "6 - WorkRequest erstellt")
        WorkManager
            .getInstance(context)
            .enqueue(request)
        Log.e("WORKER", "7 - Worker eingeplant")
    }

    suspend fun getParticipants(matchData: MatchData) {

        val team1 = RetrofitEtf2lInstance.etf2l_api.getTeam(id = matchData.match.clan1.id)
        matchData.team1.players = team1.team.players
        val team2 = RetrofitEtf2lInstance.etf2l_api.getTeam(id = matchData.match.clan2.id)
        matchData.team2.players = team2.team.players

    }

}
