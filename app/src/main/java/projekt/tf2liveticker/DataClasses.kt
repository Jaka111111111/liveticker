package projekt.tf2liveticker

import kotlinx.serialization.Serializable
import kotlin.Int

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
    //val name: String,
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

data class TeamResponse(
    val team: Team
)
data class MatchesResponse(
    val results: Results,
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