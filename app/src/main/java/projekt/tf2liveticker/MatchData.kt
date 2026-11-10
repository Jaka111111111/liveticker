package projekt.tf2liveticker

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
class MatchData(
    val match: Match,
    var bestOf: Int = 3,
    var mapsNeededToWin: Int = (bestOf / 2) + 1,
    var team1score: Int = 0,
    var team2score: Int = 0,
    val team1: TeamData = TeamData(id = match.clan1.id),
    val team2: TeamData = TeamData(id = match.clan2.id),
    var selected: Boolean = false,
    var matchEnded: Boolean = false,
    var dateString: String = "",
    var tiesAllowed: Boolean = true,
    var workerActive: Boolean = false,
    var mapEndedReason: String = ""
) {
    var observableSelected by mutableStateOf(false)
    var mutableMapDataList = mutableListOf<MapData>()

    fun logIDNotInUse(LogID: Int): Boolean {
        if (mutableMapDataList.isEmpty()) {
            return true
        }
        else {
            for (map in mutableMapDataList) {
                if (map.logID == LogID) {
                    return false
                }
            }
            return true
        }
    }
    fun getDateStringFormatted(): String {
        if (this.dateString == "") {
            this.dateString = CalendarHelper().buildDateStringDDMMYY(match.time)
        }
        return this.dateString
    }

    fun getLatestMap(): MapData {
        if (mutableMapDataList.isEmpty()) {
            mutableMapDataList.add(MapData())
            return mutableMapDataList.first()
        } else {
            return mutableMapDataList.last()
        }
    }

    fun mapInfoStringBuilder(mapData: MapData = getLatestMap()): String {
        val minutes = if (mapData.length / 60 < 10) {
            "0"
        } else {
            ""
        } + mapData.length / 60
        val seconds = if (mapData.length % 60 < 10) {
            "0"
        } else {
            ""
        } + mapData.length % 60 //arrrrrgh
        val mapScore =
            (if (mapData.blueTeamID == this.team1.id) {
                mapData.blueScore.toString() + " - " + mapData.redScore
            } else {
                mapData.redScore.toString() + " - " + mapData.blueScore
            })
        val ended = (if (mapData.mapEnded) {
            " final score on " + mapData.name + " after "
        } else {
            " score on "+ mapData.name +" after "
        }) + minutes + ":" + seconds
        return mapScore + ended + mapEndedReason
    }

    fun matchInfoStringBuilder(): String {
        var matchInfo = ""
        mutableMapDataList.forEach {
            if (it.winner != "tie") {
                matchInfo += it.winner + " won on " + it.name + " " + "" +
                        (if (it.blueScore > it.redScore) {
                            it.blueScore.toString() + " - " + it.redScore
                        } else {
                            it.redScore.toString() + " - " + it.blueScore
                        }) + "\n"
            } else {
                matchInfo += it.name + " tied: " + it.redScore.toString() + " - " + it.blueScore
            }
        }
        return matchInfo
    }

    suspend fun checkRecentLogs() {
        val randID = getPlayerCombination()[0].steam?.id64
        val response = RetrofitLogInstance.log_api.getRecentLogs(randID)
        Log.d("checkRecentLogs", "${response.logs.first()}")
        if (response.logs.first().id > getLatestMap().logID) {
            getLatestMap().mapEnded = true
            this.mapEndedReason = "RecentLogFound"
        }
    }

    suspend fun updateMapScore() {

        var latestMap = getLatestMap()

        val response = RetrofitLogInstance.log_api.getResults(latestMap.logID)
        latestMap.updated = false
        latestMap.lastUpdated = response.info.date * 1000

        if (latestMap.length != response.length) {
            latestMap.updated = true
            //latestMap.lastUpdated = System.currentTimeMillis()

            if (response.teams["Red"]?.score!! + response.teams["Blue"]?.score!! == latestMap.redScore + latestMap.blueScore
                && response.rounds.last().winner != null
            ) {
                latestMap.mapEnded = true
                this.mapEndedReason = "LogUpdateWithoutScoreChangeOrRoundReset"
            }
        }
        //
        latestMap.length = response.length
        latestMap.redScore = response.teams["Red"]?.score!!
        latestMap.blueScore = response.teams["Blue"]?.score!!


        if (latestMap.redScore == 5 || latestMap.blueScore == 5) {
            latestMap.mapEnded = true
            this.mapEndedReason = "ScoreLimitReached"
        }

        if (System.currentTimeMillis() - latestMap.lastUpdated > MsToMinute.minutes5) {
            checkRecentLogs()
        }

        if (System.currentTimeMillis() - latestMap.lastUpdated > MsToMinute.minutes30) {
            latestMap.mapEnded = true
            this.mapEndedReason = "TimeoutReached"
        }

        if (latestMap.mapEnded) {
            if (!latestMap.mapConsidered) {
                latestMap.mapConsidered = true
                updateMatchScore(latestMap)
            }
        }


    }

    fun updateTeamForColor(color: String, mapData: MapData) {
        if (color == "red") {
            if (this.team1.id == mapData.redTeamID) {
                this.team1score++
                mapData.winner = this.match.clan1.name
            } else {
                this.team2score++
                mapData.winner = this.match.clan2.name
            }
        } else {
            if (this.team1.id == mapData.blueTeamID) {
                this.team1score++
                mapData.winner = this.match.clan1.name
            } else {
                this.team2score++
                mapData.winner = this.match.clan1.name
            }
        }
    }

    fun updateMatchScore(mapData: MapData) {

            if (mapData.redScore == mapData.blueScore) {
                mapData.winner = "tie"
                if (this.tiesAllowed) {
                    team1score++
                    team2score++
                }
            } else if (mapData.redScore > mapData.blueScore) {
                updateTeamForColor("red", mapData)
            } else {
                updateTeamForColor("blue", mapData)
            }

            if (team1score == mapsNeededToWin
                || team2score == mapsNeededToWin
            ) {
                matchEnded = true
            } else {
                mutableMapDataList.add(MapData())
            }

    }

    fun getPlayerCombination(): List<PlayerLog> {

        var playerList: List<PlayerLog> = emptyList()
//        for (player in team2.players+team1.players) {
//            if (player.role == "Leader") {
//                playerList.apply { player }
//            }
        //}
        Log.d("getPlayerCombination", "team 1: ${team1.players}")
        Log.d("getPlayerCombination", "team 2: ${team2.players}")
        if (team1.players.size == 1 && team2.players.size == 1) { //testing purposes :DDD
            return listOf(
                team1.players[0],
                team2.players[0]
            )
        } else {
            return listOf(
                team1.players[Random.nextInt(team1.players.size - 1)],
                team2.players[Random.nextInt(team2.players.size - 1)]
            )
        }

    }

    fun containsFilter(filter: String): Boolean {
        if ((match.clan1.name + match.clan2.name
                    + match.competition
                    + match.division).uppercase()
                .contains(filter.uppercase())
        ) {
            return true
        } else {
            return false
        }
    }
}