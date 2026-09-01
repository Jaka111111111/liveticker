package projekt.tf2liveticker.Models

import android.health.connect.datatypes.units.Length
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlin.Int

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

@Serializable
class MatchData(
    val match: Match,
    var players_team1: List<PlayerLog> = emptyList(),
    var players_team2: List<PlayerLog> = emptyList(),
    var team1_colour: String? = "red",
    var team2_colour: String? = "blue",
    var red_score: Int = 0,
    var blue_score: Int = 0,
    var length: Int = 0,
    var oldLength: Int = 0
) {
    var selected by mutableStateOf(false)
    var livelog by mutableIntStateOf(0)

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
    val logs: logs
)

data class LogResponse(
    val version: Int,
    val teams: Map<String, TeamStats>,
    val players: Map<String, PlayerStats>,
    val names: Map<String, String>,
    val success: Boolean,
    val length: Int
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