package projekt.tf2liveticker.Models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import projekt.tf2liveticker.RetrofitLogInstance
import kotlin.Int
import kotlin.random.Random
import kotlin.text.get

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
    var id: Int = 0,
    var players: List<PlayerLog> = emptyList(),
    var score: Int = 0,
)
@Serializable
data class MapData(
    val name: String = "map",
    var blue_score: Int = 0,
    var red_score: Int = 0,
    var red_team: Int = 0,
    var blue_team: Int = 0,
    var length: Int = 0,
    var LogID: Int = 0,
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
    var red_score: Int = 0,
    var blue_score: Int = 0,
    val team1: TeamData = TeamData(id = match.clan1.id),
    val team2: TeamData = TeamData(id = match.clan2.id),
    var selected: Boolean = false,
    var matchEnded: Boolean = false
) {
    var observableSelected by mutableStateOf(false)
    var mutableMapDataList = mutableListOf<MapData>()

    fun getLatestMap(): MapData {
        if (mutableMapDataList.isEmpty()) {
            mutableMapDataList.add(MapData())
            return mutableMapDataList.first()
        }
        else {
            return mutableMapDataList.last()
        }
    }

    suspend fun updateMapScore() {
        var latestMap = getLatestMap()

        val response = RetrofitLogInstance.log_api.getResults(latestMap.LogID)
        latestMap.updated = false

        if (latestMap.length != response.length) {
            latestMap.updated = true
            latestMap.lastUpdated = System.currentTimeMillis()

            //CheckIfMapEnded()
            if (latestMap.red_score + latestMap.blue_score == response.teams["Red"]?.score!! + response.teams["Blue"]?.score!!) {
                latestMap.mapEnded = true
            }


        }
        if ((System.currentTimeMillis() - latestMap.lastUpdated) / 1000 > 1200) {
            latestMap.mapEnded = true
        }
        //
        latestMap.length = response.length
        latestMap.red_score = response.teams["Red"]?.score!!
        latestMap.blue_score = response.teams["Blue"]?.score!!
        if (latestMap.mapEnded) {
            updateMatchScore(latestMap)
        }
    }

    fun updateMatchScore(mapData: MapData) {
        if (mapData.red_score == mapData.blue_score) {
            mapData.winner = "tie"
        }
        else if (mapData.red_score > mapData.blue_score) {
            mapData.winner = "red"
            this.red_score++
        }
        else {
            mapData.winner = "blue"
            this.blue_score++
        }

        if (mapData.mapEnded && (red_score == mapsNeededToWin
                             || blue_score == mapsNeededToWin)) {
            matchEnded = true
        }
        else {
            mutableMapDataList.add(MapData())
        }
    }

    fun getPlayerCombination(): List<PlayerLog> {

        var playerList: List<PlayerLog> = emptyList()
        for (player in team2.players+team1.players) {
            if (player.role == "Leader") {
                playerList.apply { player }
            }
        }
        return listOf(team1.players[Random.nextInt(team1.players.size)],
            team2.players[Random.nextInt(team2.players.size)])
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
    val success: Boolean,
    val length: Int,
    val info: LogInfo
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