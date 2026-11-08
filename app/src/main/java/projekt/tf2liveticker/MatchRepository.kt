package projekt.tf2liveticker

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class MatchRepository() {

    var matchData: List<MatchData> = emptyList()
        private set

    var matches: List<Match> = emptyList()
        private set

    suspend fun tryRandomCombination(matchData: MatchData): RecentLogResponse {
        val list = matchData.getPlayerCombination()
        Log.d("findLiveLog", "Spieler: ${list[0].name} ${list[0].steam?.id64}") //und ${list[1].name}")
        return RetrofitLogInstance.log_api.getRecentLogs(list[0].steam?.id64) //+ "," + list[1].steam?.id64)
    }

    suspend fun findLiveLog(matchData: MatchData) {

        if (matchData.mutableMapDataList.last().logID == 0) {

            if (matchData.team1.players.isEmpty() || matchData.team2.players.isEmpty()) {
                getParticipants(matchData)
            }

            val randomPlayerRequest = tryRandomCombination(matchData)

            Log.d("findLiveLog", "Ergebnisse: ${randomPlayerRequest.logs.size}")
            Log.d("findLiveLog", "Ergebnisse: ${randomPlayerRequest}")

            if (randomPlayerRequest.logs.isEmpty()) {
                findLiveLog(matchData)
            }
            else {

                val latest =
                    RetrofitLogInstance.log_api.getResults(randomPlayerRequest.logs.first().id)

                Log.d(
                    "findLiveLog",
                    "Log aktuell genug?: ${(latest.info.date > matchData.match.time)}"
                )


                var relevantPlayerCounter = 0

                Log.d("findLiveLog", "Ausgewähltes Log: ${latest}")
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
                Log.d("findLiveLog", players.size.toString())


                players.forEach { player ->
                    val id = player.steam3Id

                    matchData.team1.players.forEach { //später ändern!!!!

                        Log.d("findLiveLog", "Verglichen mit: $id")
                        val steam = it.steam
                        val s64 = steam?.id64
                        val steam3 = s64?.toLong()
                        if ("[U:1:${steam3?.minus(76561197960265728)}]" == id) {     //it.steam?.id3 == id) {
                            if (player.team == "Red") { //case sensitive..
                                matchData.mutableMapDataList.last().redTeamID = matchData.team1.id
                                matchData.mutableMapDataList.last().blueTeamID = matchData.team2.id
                            }
                            relevantPlayerCounter++
                        }
                    }

                    matchData.team2.players.forEach {
                        if ("[U:1:${it.steam?.id64?.toLong()?.minus(76561197960265728)}]" == id) { //it.steam?.id3 == id) {
                            if (player.team == "Red") {
                                matchData.mutableMapDataList.last().redTeamID = matchData.team2.id
                                matchData.mutableMapDataList.last().blueTeamID = matchData.team1.id
                            }
                            relevantPlayerCounter++
                        }
                    }
                }

                Log.d("findLiveLog", "Relevante Spieler: ${relevantPlayerCounter}")

                if (relevantPlayerCounter >= 8
                    ||
                    relevantPlayerCounter == (matchData.team2.players.size+matchData.team1.players.size))
                {
                    matchData.mutableMapDataList.last().logID = randomPlayerRequest.logs[0].id
                    matchData.mutableMapDataList.last().name = randomPlayerRequest.logs[0].map
                }
            }
        }
    }

    suspend fun refreshResultsDummy(context: Context, playerIDs: String) {
        val c1 = Clan("England", 0, "Placeholder")
        val c2 = Clan("England", 1, "Holdplacer")
        val com = Competition("Placeholder", 2, "Placeholder", "6v6")
        val d = Division(9, "hi", 1, 1)
        val ms = emptyList<String>()
        val m = Match(0, c1, c2, com, d, ms, "1", System.currentTimeMillis() / 1000)
        val md = MatchData(m, bestOf = 1, team1 = TeamData(name = "BBBB", id = 0), team2 = TeamData(name = "A",id = 1))
        md.selected = true
        val players = playerIDs.split(",")
        md.team1.players = mutableListOf<PlayerLog>(PlayerLog("", 0, "", "", PlayerSteam("", "", "", players[0]), ""))
        md.team2.players = mutableListOf<PlayerLog>(PlayerLog("", 0, "", "", PlayerSteam("", "", "", players[1]), ""))
        //md.mutableMapDataList.add(MapData(LogID = 0))
        this.matchData += md
        val storageRepository =
            MatchStorageRepository(context)
        storageRepository.saveMatch(md)

        scheduleMatch(context, m)

    }

    suspend fun refreshResults(matchData: MatchData?) {
        if (matchData!!.selected) { //&& matchData.match.time * 1000L < System.currentTimeMillis()
            if (matchData.getLatestMap().logID == 0) {
                findLiveLog(matchData)
            }
            val latest = matchData.mutableMapDataList.last()
            if (latest.logID != 0 && (System.currentTimeMillis() - latest.lastUpdated) > 30000) {

                matchData.updateMapScore()

            }
        }
    }

    suspend fun getLocalMatches(context: Context) {
        val storageRepository = MatchStorageRepository(context)
        matchData = storageRepository.getAllMatches()
    }

    suspend fun getEtf2lMatches(context: Context, page: Int) {
        val storageRepository = MatchStorageRepository(context)
        val response = RetrofitEtf2lInstance.etf2l_api.getMatches(page)
        matches = response.results.data
        for (m in matches) {
            storageRepository.saveMatch(MatchData(m, team1 = TeamData(), team2 = TeamData()))
        }
        storageRepository.addDummyData()
        matchData = storageRepository.getAllMatches()
    }

    fun scheduleMatch(
        context: Context,
        match: Match,
    ) {

        val matchTimeMillis = match.time * 1000L
        var delay = matchTimeMillis - System.currentTimeMillis()


        if (delay < 0) {
            delay = MsToMinute.seconds30
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
        Log.d(
            "Schedule Worker", "Worker arbeitet in ${delay.milliseconds.inWholeHours} Stunden, " +
                    "${delay.milliseconds.inWholeMinutes % 60} Minuten und ${delay.milliseconds.inWholeSeconds % 1200} Sekunden"
        )
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
