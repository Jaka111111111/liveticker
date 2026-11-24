package projekt.tf2liveticker

import android.content.Context
import android.util.Log
import projekt.tf2liveticker.alarm_manager.AlarmScheduler
import projekt.tf2liveticker.api_callers.RetrofitEtf2lInstance
import projekt.tf2liveticker.api_callers.RetrofitLogInstance

class MatchRepository() {

    var matchDataList: List<MatchData> = emptyList()
        private set

    var matches: List<Match> = emptyList()
        private set

    var appSettings: AppSettings = AppSettings()

    suspend fun tryRandomCombination(matchData: MatchData): RecentLogResponse {
        val list = matchData.getPlayerCombinationFromTeams()
        Log.d("findLiveLog", "Spieler: ${list[0].name} ${list[0].steam?.id64}") //und ${list[1].name}")
        return RetrofitLogInstance.log_api.getRecentLogs(list[0].steam?.id64) //+ "," + list[1].steam?.id64)
    }

    suspend fun findLiveLog(matchData: MatchData) {

        if (matchData.mutableMapDataList.last().logID == 0) {

            if (matchData.team1data.players.isEmpty() || matchData.team2data.players.isEmpty()) {
                getParticipants(matchData)
            }

            val randomPlayerRequest = tryRandomCombination(matchData)

            Log.d("findLiveLog", "Ergebnisse: ${randomPlayerRequest.logs.size}")
            Log.d("findLiveLog", "Ergebnisse: ${randomPlayerRequest}")

            if (randomPlayerRequest.logs.isEmpty()) {
                findLiveLog(matchData)
            }
            else {
                if (matchData.logIDNotInUse(randomPlayerRequest.logs.first().id)) {


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


                    players.forEach { player -> //extrahieren!!
                        val id = player.steam3Id

                        matchData.team1data.players.forEach { //später ändern!!!!

                            Log.d("findLiveLog", "Verglichen mit: $id")
                            val steam = it.steam
                            val s64 = steam?.id64
                            val steam3 = s64?.toLong()
                            if ("[U:1:${steam3?.minus(76561197960265728)}]" == id) {     //it.steam?.id3 == id) {
                                if (player.team == "Red") { //case sensitive..
                                    matchData.mutableMapDataList.last().redTeamID =
                                        matchData.team1data.id
                                    matchData.mutableMapDataList.last().blueTeamID =
                                        matchData.team2data.id
                                }
                                relevantPlayerCounter++
                                matchData.participatingPlayers.add(it)
                            }
                        }

                        matchData.team2data.players.forEach {
                            if ("[U:1:${
                                    it.steam?.id64?.toLong()?.minus(76561197960265728)
                                }]" == id
                            ) { //it.steam?.id3 == id) {
                                if (player.team == "Red") {
                                    matchData.mutableMapDataList.last().redTeamID =
                                        matchData.team2data.id
                                    matchData.mutableMapDataList.last().blueTeamID =
                                        matchData.team1data.id
                                }
                                relevantPlayerCounter++
                                matchData.participatingPlayers.add(it)
                            }
                        }
                    }

                    Log.d("findLiveLog", "Relevante Spieler: ${relevantPlayerCounter}")

                    if (relevantPlayerCounter >= 8
                        ||
                        relevantPlayerCounter == (matchData.team2data.players.size + matchData.team1data.players.size)
                    ) {
                        matchData.mutableMapDataList.last().logID = randomPlayerRequest.logs[0].id
                        matchData.mutableMapDataList.last().name = randomPlayerRequest.logs[0].map
                    }
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
        val players = playerIDs.split(",")
        val m = Match(matchDataList.size, c1, c2, com, d, ms, "1", System.currentTimeMillis() / 1000)
        val md = MatchData(m, bestOf = 1, mapsNeededToWin = 1, team1data = TeamData(id = 0), team2data = TeamData(id = 1))
        md.selected = true
        md.team1data.players = mutableListOf<PlayerLog>(PlayerLog("", 0, "", "", PlayerSteam("", "", "", players[0]), ""))
        md.team2data.players = mutableListOf<PlayerLog>(PlayerLog("", 0, "", "", PlayerSteam("", "", "", players[1]), ""))
        this.matchDataList += md
        val storageRepository =
            MatchStorageRepository(context)
        storageRepository.saveMatch(md)

        scheduleMatch(context, m)

    }

    suspend fun refreshResults(matchData: MatchData?, context: Context) {
        if (matchData!!.selected) {
            if (matchData.getLatestMap().logID == 0) {
                findLiveLog(matchData)
            }
            val latest = matchData.mutableMapDataList.last()
            if (latest.logID != 0 && (System.currentTimeMillis() - latest.lastUpdated) > 30000) {

                matchData.updateMapScore(context)

            }
        }
    }

    suspend fun getAppStats(context: Context) {
        val storageRepository = MatchStorageRepository(context)
        appSettings = storageRepository.getAppStats()
    }

    suspend fun saveAppStats(context: Context, appSettings: AppSettings) {
        val storageRepository = MatchStorageRepository(context)
        this.appSettings = appSettings
        storageRepository.updateAppStats(appSettings)
    }

    suspend fun getLocalMatches(context: Context) {
        val storageRepository = MatchStorageRepository(context)
        matchDataList = storageRepository.getAllMatches()
    }

    suspend fun getEtf2lMatches(context: Context, page: Int) {
        val storageRepository = MatchStorageRepository(context)
        val response = RetrofitEtf2lInstance.etf2l_api.getMatches(page)
        matches = response.results.data
        storageRepository.updateMatches(matches)
        matchDataList = storageRepository.getAllMatches()
    }

    fun scheduleMatch(
        context: Context,
        match: Match,
    ) {

        var matchTimeMillis = match.time * 1000L
        if (matchTimeMillis < System.currentTimeMillis()) {
            matchTimeMillis = System.currentTimeMillis()
        }

        AlarmScheduler().scheduleMatchAlarm(context, matchTimeMillis)

//        val request = OneTimeWorkRequestBuilder<MatchUpdateWorker>()
//            .setInitialDelay(delay, TimeUnit.MILLISECONDS) //DELAY
//            .setInputData(inputData)
//            .addTag("LiveTicker")
//            .build()
//
//        WorkManager
//            .getInstance(context)
//            .enqueueUniqueWork("Match_" + match.id, ExistingWorkPolicy.REPLACE, request)

    }

    suspend fun getParticipants(matchData: MatchData) {

        val team1 = RetrofitEtf2lInstance.etf2l_api.getTeam(id = matchData.match.clan1.id)
        matchData.team1data.players = team1.team.players
        val team2 = RetrofitEtf2lInstance.etf2l_api.getTeam(id = matchData.match.clan2.id)
        matchData.team2data.players = team2.team.players

    }

}
