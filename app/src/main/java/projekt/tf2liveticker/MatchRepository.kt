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
import projekt.tf2liveticker.Models.RecentLogResponse
import projekt.tf2liveticker.Models.TeamData
import java.util.concurrent.TimeUnit

class MatchRepository() {

    var matchData: List<MatchData> = emptyList()
        private set

    var matches: List<Match> = emptyList()
        private set

    suspend fun tryRandomCombination(matchData: MatchData): RecentLogResponse {
        val list = matchData.getPlayerCombination()
        Log.d("findLiveLog", "Spieler: ${list[0].name} und ${list[1].name}")
        return RetrofitLogInstance.log_api.getRecentLogs(list[0].steam?.id64 +","+list[1].steam?.id64)
    }

    suspend fun findLiveLog(matchData: MatchData) {

        if (matchData.mutableMapDataList.last().LogID == 0) {

            if(matchData.team1.players.isEmpty()) {
                getParticipants(matchData)
            }

            val randomPlayerRequest = tryRandomCombination(matchData)

            Log.d("findLiveLog", "Ergebnisse: ${randomPlayerRequest.logs.size}")
            Log.d("findLiveLog", "Ergebnisse: ${randomPlayerRequest}")

            val latest = RetrofitLogInstance.log_api.getResults(randomPlayerRequest.logs.first().id)

            Log.d("findLiveLog", "Log Alter: ${(latest.info.date)}")
            Log.d("findLiveLog", "Match Zeitpunkt: ${(matchData.match.time)}")
            Log.d("findLiveLog", "Log aktuell genug?: ${(latest.info.date > matchData.match.time)}")


            if (latest.info.date > matchData.match.time) {

                matchData.mutableMapDataList.last().LogID = randomPlayerRequest.logs.first().id

//                var counter = 0
//
//                Log.d("findLiveLog", "Ausgewähltes Log: ${latest}")
//                val players = latest.players.map { (steam3Id, stats) ->
//
//                    Player(
//                        steam3Id = steam3Id,
//                        name = latest.names[steam3Id] ?: "Unknown",
//                        team = stats.team,
//                        kills = stats.kills,
//                        deaths = stats.deaths,
//                        assists = stats.assists,
//                        damage = stats.dmg
//                    )
//                }
//
//                players.forEach { player ->
//                    val id = player.steam3Id
//
//                    matchData.team1.players.forEach {
//                        if (it.steam?.id3 == id) {
//                            if (player.team == "red") {
//                                matchData.mutableMapDataList.last().red_team = matchData.team1.id
//                                matchData.mutableMapDataList.last().blue_team = matchData.team2.id
//                            }
//                            counter++
//                        }
//                    }
//
//                    matchData.team2.players.forEach {
//                        if (it.steam?.id3 == id) {
//                            if (player.team == "red") {
//                                matchData.mutableMapDataList.last().red_team = matchData.team2.id
//                                matchData.mutableMapDataList.last().blue_team = matchData.team1.id
//                            }
//                            counter++
//                        }
//                    }
                }

//                Log.d("findLiveLog", "Relevante Spieler: ${counter}")
//
//                if (counter >= 8) {
//                    matchData.mutableMapDataList.last().LogID = randomPlayerRequest.logs[0].id
//                }

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
        val md = MatchData(m, bestOf = 2, team1 = TeamData(id = 0), team2 = TeamData(1))
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

                matchData.updateMapScore()

            }
        }
    }

    suspend fun getMatches(context: Context, page: Int) {
        val storageRepository = MatchStorageRepository(context)
        val response = RetrofitEtf2lInstance.etf2l_api.getMatches(page)
        matches = response.results.data
        for (m in matches) {
            storageRepository.saveMatch(MatchData(m, team1 = TeamData(), team2 = TeamData()))
        }
        storageRepository.addDummyData()
        matchData = storageRepository.getAllMatches()
//        matchData.first().observableSelected

    }

    fun scheduleMatch(
        context: Context,
        match: Match,
    ) {

        val matchTimeMillis = match.time * 1000L
        var delay = matchTimeMillis - System.currentTimeMillis()

        if (delay < 0) {
            delay = System.currentTimeMillis() + 3000
        }
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
        val inputData = workDataOf(
            "MATCH_ID" to match.id
        )
        val request = OneTimeWorkRequestBuilder<MatchUpdateWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS) //DELAY
            .setInputData(inputData)
//            .setConstraints(constraints)
            .build()
        WorkManager
            .getInstance(context)
            .enqueue(request)
    }

    suspend fun getParticipants(matchData: MatchData) {

        val team1 = RetrofitEtf2lInstance.etf2l_api.getTeam(id = matchData.match.clan1.id)
        matchData.team1.players = team1.team.players
        val team2 = RetrofitEtf2lInstance.etf2l_api.getTeam(id = matchData.match.clan2.id)
        matchData.team2.players = team2.team.players

    }

}
