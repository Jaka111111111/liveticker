package projekt.tf2liveticker

data class User(
    val id: Int,
    val steamid: Int,
    val name: String,
)

data class Match(
    val id: Int,
    val clan1: Clan,
    val clan2: Clan,
    val competition: Competition,
    val division: Division,
    val maps: List<String>,
    val round: String,
    val time: Long
)

data class MatchViewModel(
    val Match: Match,
    var Selected: Boolean = false
)

data class Clan(
    val country: String,
    val id: Int,
    val name: String
)

data class Competition(
    val category: String,
    val id: Int,
    val name: String,
    val type: String
)

data class Division(
    val id: Int,
    val name: String,
    val skill_contrib: Int,
    val tier: Int
)

data class ApiResponse(
    val results: Results
)

data class Results(
    val data: List<Match>
)