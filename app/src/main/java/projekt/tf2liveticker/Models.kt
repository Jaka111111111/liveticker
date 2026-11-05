package projekt.tf2liveticker

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlin.Int
import kotlin.random.Random

object MsToMinute {
    val minutes30: Long = 1800000
    val minutes10: Long = 600000
    val minutes5: Long = 300000
    val minute1: Long = 60000
    val seconds30: Long = 30000
}

object SecToMinute {
    val minutes4: Long = 240
    val minutes5: Long = 300
}

@Serializable
data class PlayerLog( //etf2l
    val country: String?,
    val id: Int,
    val name: String,
    val role: String?,
    val steam: PlayerSteam?,
    val url: String?
)

@Serializable
data class Player( //logs
    val steam3Id: String,
    val name: String,
    val team: String,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val damage: Int
)

@Serializable
data class PlayerSteam(
    val avatar: String?,
    val id: String?,
    val id3: String?,
    val id64: String?
)

@Serializable
data class MatchDataList(
    val MatchData : List<MatchData> = emptyList()
)

@Serializable data class TeamData(
    var name: String = "",
    var id: Int = 0,
    var players: List<PlayerLog> = emptyList()
)
@Serializable
data class MapData(
    var name: String = "map",
    var blueScore: Int = 0,
    var redScore: Int = 0,
    var redTeamID: Int = 0,
    var blueTeamID: Int = 0,
    var length: Int = 0,
    var logID: Int = 0,
    var updated: Boolean = false,
    var mapEnded: Boolean = false,
    var winner: String = "",
    var lastUpdated: Long = 0
)
@Serializable
class MatchData(
    val match: Match,
    var bestOf: Int = match.maps.size,
    var mapsNeededToWin: Int = (bestOf / 2) + 1,
    var team1score: Int = 0,
    var team2score: Int = 0,
    val team1: TeamData = TeamData(id = match.clan1.id),
    val team2: TeamData = TeamData(id = match.clan2.id),
    var selected: Boolean = false,
    var matchEnded: Boolean = false,
    var dateString: String = "",
    var tiesAllowed: Boolean = true
) {
    var observableSelected by mutableStateOf(false)
    var mutableMapDataList = mutableListOf<MapData>()

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

    fun mapInfoStringBuilder(): String {
        var mapInfo = ""
        mutableMapDataList.forEach {
            mapInfo += it.winner + " won on " + it.name + " " + it.blueScore + " - " + it.redScore
        }
        return mapInfo
    }

    suspend fun checkRecentLogs() {
        val randID = getPlayerCombination()[0].steam?.id64
        val response = RetrofitLogInstance.log_api.getRecentLogs(randID)
        Log.d("checkRecentLogs", "${response.logs.first()}")
        if (response.logs.first().id > getLatestMap().logID) {
            getLatestMap().mapEnded = true
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
                && response.rounds.last().winner != null) {
                latestMap.mapEnded = true
            }
        }
        //
        latestMap.length = response.length
        latestMap.redScore = response.teams["Red"]?.score!!
        latestMap.blueScore = response.teams["Blue"]?.score!!


        if (latestMap.redScore == 5 || latestMap.blueScore == 5) {
            latestMap.mapEnded = true
        }

        if (System.currentTimeMillis() - latestMap.lastUpdated > MsToMinute.minutes5) {
            checkRecentLogs()
        }

        if (System.currentTimeMillis() - latestMap.lastUpdated > MsToMinute.minutes30) {
            latestMap.mapEnded = true
        }

        if (latestMap.mapEnded) {
            updateMatchScore(latestMap)
        }
    }

    fun updateTeamForColor(color: String, mapData: MapData) {
        if (color == "red") {
            if (this.team1.id == mapData.redTeamID) {
                this.team1score++
                mapData.winner = this.team1.name
            }
            else {
                this.team2score++
                mapData.winner = this.team2.name
            }
        }
        else {
            if (this.team1.id == mapData.blueTeamID) {
                this.team1score++
                mapData.winner = this.team1.name
            }
            else {
                this.team2score++
                mapData.winner = this.team2.name
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
            || team2score == mapsNeededToWin)
         {
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
        if (team1.players.size == 1 && team2.players.size == 1) {
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
}
@Serializable
data class Match(
    val id: Int,
    val clan1: Clan,
    val clan2: Clan,
    val competition: Competition,
    val division: Division,
    val maps: List<String>,
    val round: String,
    var time: Long
)
@Serializable
data class Clan(
    val country: String,
    val id: Int,
    val name: String
)
@Serializable
data class Team(
    val id: Int,
    val name: String,
    val country: String?,
    val competitions: Map<String, Competition>,
    val players: List<PlayerLog>
)
@Serializable
data class Competition(
    val category: String,
    val id: Int,
    val name: String,
    val type: String
)
@Serializable
data class Division(
    val id: Int,
    val name: String,
    val skill_contrib: Int,
    val tier: Int
)

data class ApiResponse(
    val results: Results,
    val team: Team,
)

//data class Status(
//
//)
data class TeamResponse(
    //val status: Status,
    val team: Team
)
data class MatchesResponse(
    val results: Results,
    //val status: Status
)
data class RecentLogResponse(
    val success: Boolean,
    val results: Int,
    val total: Int,
    val parameters: LogParameters,
    val logs: List<log> = emptyList()
)
data class LogInfo(
    val map: String,
    val date: Long
)
data class LogParameters(
    val player: String,
    val uploader: String? = null,
    val title: String? = null,
    val map: String? = null,
    val limit: Int,
    val offset: Int
)

data class LogResponse(
    val version: Int,
    val teams: Map<String, TeamStats>,
    val players: Map<String, PlayerStats>,
    val names: Map<String, String>,
    val rounds: Array<LogRound>,
    val success: Boolean,
    val length: Int,
    val info: LogInfo
    )

@Serializable
data class LogRound(
    val start_time: Long,
    val winner: String
)

@Serializable
data class TeamStats (
    val score: Int,
    val kills: Int,
    val deaths: Int,
    val dmg: Int
)

@Serializable
data class PlayerStats(
    val team: String,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val dmg: Int
)

@Serializable
data class logs(
    val logs: List<log>
)

@Serializable
data class log(
    val id: Int,
    val title: String,
    val map: String,
    val date: Int,
    val views: Int,
    val players: Int
)

@Serializable
data class Results(
    val data: List<Match>
)