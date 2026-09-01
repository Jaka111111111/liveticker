package projekt.tf2liveticker

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import projekt.tf2liveticker.Models.Clan
import projekt.tf2liveticker.Models.Competition
import projekt.tf2liveticker.Models.Division
import projekt.tf2liveticker.Models.Match
import projekt.tf2liveticker.Models.MatchData
import projekt.tf2liveticker.Models.MatchDataList
import projekt.tf2liveticker.Models.Player
import java.util.concurrent.TimeUnit

class MatchRepository() {

    var matchData: List<MatchData> = emptyList()
        private set

    var matches: List<Match> = emptyList()
        private set

//    suspend fun getTeam(id: Int): ApiResponse {
//        return RetrofitEtf2lInstance.etf2l_api.getTeam(id)
//    }

    suspend fun findLiveLog(matchData: MatchData) {

        if (matchData.livelog == 0) {
            matchData.players_team1.forEach { player ->

                val response = RetrofitLogInstance.log_api.getRecentLogs(player.steam?.id)
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
                    matchData.players_team1.forEach {
                        if (it.steam?.id3 == id) {
                            matchData.team1_colour = player.team
                            counter++
                        }
                    }
                }

                if (counter >= 8) {
                    matchData.livelog = response.logs.logs[0].id
                }
            }
        }
    }

    suspend fun saveMatch(
        context: Context,
        matchDataList: MatchDataList
    ) {
        //MatchStorageRepository
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
        val md = MatchData(m)
        md.selected = true
        md.livelog = logId
        this.matchData += md

        scheduleMatch(context, m)

    }

    suspend fun refreshResults(matchData: MatchData?) {
        if (matchData!!.selected) { //&& matchData.match.time * 1000L < System.currentTimeMillis()
            if (matchData.livelog == 0) {
                findLiveLog(matchData)
            }
            val response = RetrofitLogInstance.log_api.getResults(matchData.livelog)
            matchData.length = response.length
            matchData.red_score = response.teams["Red"]?.score!!
            matchData.blue_score = response.teams["Blue"]?.score!!
            //scheduleMatch(LocalContext.current, it.match.time)
        }
    }

    suspend fun getMatches() {

        val response = RetrofitEtf2lInstance.etf2l_api.getMatches()
        matches = response.results.data
        val oldViewModels = matchData.associateBy { it.match.id }
        this.matchData = response.results.data.map { match ->
            oldViewModels[match.id]
                ?: MatchData(match)
        }
    }

    suspend fun scheduleMatch(
        context: Context,
        match: Match
    ) {
        match.time += 60
        //saveMatch(context, MatchDataList(MatchData = matchData))

        val matchTimeMillis = match.time * 1000L

        val delay = matchTimeMillis - System.currentTimeMillis()

//        if (delay <= 0) {
//            return
//        }



        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            "MATCH_ID" to match.id
        )

        val request = OneTimeWorkRequestBuilder<Worker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS) //DELAY
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        WorkManager
            .getInstance(context)
            .enqueue(request)

    }

    suspend fun getParticipants(matchData: MatchData) {

        val team1 = RetrofitEtf2lInstance.etf2l_api.getTeam(id = matchData.match.clan1.id)
        matchData.players_team1 = team1.team.players
        val team2 = RetrofitEtf2lInstance.etf2l_api.getTeam(id = matchData.match.clan2.id)
        matchData.players_team2 = team2.team.players

    }

}
