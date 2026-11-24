package projekt.tf2liveticker

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import projekt.tf2liveticker.api_callers.RetrofitLogInstance
import projekt.tf2liveticker.helper_classes.CalendarHelper
import projekt.tf2liveticker.helper_classes.NotificationHelper
import kotlin.random.Random

@Serializable
class MatchData(
    var match: Match,
    var bestOf: Int = match.maps.size,
    var mapsNeededToWin: Int = (bestOf / 2) + 1,
    var team1score: Int = 0,
    var team2score: Int = 0,
    val team1data: TeamData = TeamData(id = match.clan1.id),
    val team2data: TeamData = TeamData(id = match.clan2.id),
    var selected: Boolean = false,
    var matchEnded: Boolean = false,
    var dateString: String = "",
    var mapTiesAllowed: Boolean = true,
    var matchTiesAllowed: Boolean = true,
    var mapEndedReason: String = "",
    var mapsPlayed: Int = 0,
    var participatingPlayers: ArrayList<PlayerLog> = ArrayList<PlayerLog>()
) {
    var observableSelected by mutableStateOf(false)
    var mutableMapDataList = mutableListOf<MapData>()

    fun matchStarted(): Boolean {
        return (System.currentTimeMillis() > match.time*1000)
    }
    fun logIDNotInUse(LogID: Int): Boolean {
        if (mutableMapDataList.isEmpty()) {
            return true
        } else {
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

    fun mapInfoStringBuilder(mapData: MapData = getLatestMap()) {
        if (mapData.logID == 0) {
            mapData.infoString = "Looking for log for map " + mutableMapDataList.size + "..."
        }
        else {
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

            val isLeading = if (mapData.mapEnded) {
                " won "
            } else {
                " is leading "
            }

            val mapScore =
                (if (mapData.blueScore > mapData.redScore) {
                    if (mapData.blueTeamID == this.team1data.id) {
                        this.match.clan1.name
                    } else {
                        this.match.clan2.name
                    } + isLeading + mapData.blueScore.toString() + " - " + mapData.redScore
                } else if (mapData.blueScore < mapData.redScore) {
                    if (mapData.redTeamID == this.team1data.id) {
                        this.match.clan1.name
                    } else {
                        this.match.clan2.name
                    } + isLeading + mapData.redScore.toString() + " - " + mapData.blueScore
                } else {
                    "tied " + mapData.blueScore.toString() + " - " + mapData.redScore
                })
            val mapInfo = " on " + mapData.name + " after " + minutes + ":" + seconds

            mapData.infoString = "$mapScore$mapInfo" + if(mapData.mapEnded) {"\n"} else {""}
        }
    }


    fun matchInfoStringBuilder(): String {
        var matchInfo = ""
        mutableMapDataList.forEach {
            mapInfoStringBuilder(it)
            matchInfo += it.infoString
        }
        return matchInfo
    }

    suspend fun checkRecentLogs() {
        val randID = getRandomParticipant().steam?.id64
        val response = RetrofitLogInstance.log_api.getRecentLogs(randID)
        Log.d("checkRecentLogs", "${response.logs.first()}")
        if (response.logs.first().id > getLatestMap().logID) {
            getLatestMap().mapEnded = true
            this.mapEndedReason = "RecentLogFound"
        }
    }

    suspend fun updateMapScore(context: Context) {

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

        if (latestMap.updated) {
            buildNotification(context)
        }

        if (latestMap.redScore == 5 || latestMap.blueScore == 5) {
            latestMap.mapEnded = true
            this.mapEndedReason = "ScoreLimitReached"
        }

        if (System.currentTimeMillis() - latestMap.lastUpdated > MinuteToMS.MINUTES_05) {
            checkRecentLogs()
        }

        if (System.currentTimeMillis() - latestMap.lastUpdated > MinuteToMS.MINUTES_30) {
            latestMap.mapEnded = true
            this.mapEndedReason = "TimeoutReached"
        }

        if (latestMap.mapEnded) {
            if (!latestMap.mapConsidered) {
                latestMap.mapConsidered = true
                mapsPlayed++
                updateMatchScore(latestMap, context)
            }
        }

    }

    fun buildNotification(context: Context) {
        NotificationHelper.showMatchNotification(
            context = context,
            title = if (matchEnded) {
                "${match.clan1.name} vs ${match.clan2.name} concluded! \n $team1score - $team2score"
            } else {
                "${match.clan1.name} vs ${match.clan2.name} \n $team1score - $team2score"
            },
            message = matchInfoStringBuilder(),
            notificationId = match.id,
            onGoing = !matchEnded
        )
    }

    fun updateTeamForColor(color: String, mapData: MapData) {
        if (color == "red") {
            if (this.team1data.id == mapData.redTeamID) {
                this.team1score ++
                mapData.winner = this.match.clan1.name
            } else {
                this.team2score ++
                mapData.winner = this.match.clan2.name
            }
        } else {
            if (this.team1data.id == mapData.blueTeamID) {
                this.team1score ++
                mapData.winner = this.match.clan1.name
            } else {
                this.team2score ++
                mapData.winner = this.match.clan1.name
            }
        }
    }

    fun updateMatchScore(mapData: MapData, context: Context) {

        if (mapData.redScore == mapData.blueScore) {
            mapData.winner = "tie"
            if (this.mapTiesAllowed) {
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
        }

        if (this.matchTiesAllowed && mapsPlayed == bestOf) {
            matchEnded = true
        }

        buildNotification(context)

        if (!matchEnded) {
            mutableMapDataList.add(MapData())
        }

    }

    fun getRandomParticipant(): PlayerLog {
        return participatingPlayers[Random.nextInt(participatingPlayers.size-1)]
    }

    fun getPlayerCombinationFromTeams(): List<PlayerLog> {
        Log.d("getPlayerCombination", "team 1: ${team1data.players}")
        Log.d("getPlayerCombination", "team 2: ${team2data.players}")
        if (team1data.players.size == 1 && team2data.players.size == 1) { //testing purposes :DDD
            return listOf(
                team1data.players[0],
                team2data.players[0]
            )
        } else {
            return listOf(
                team1data.players[Random.nextInt(team1data.players.size - 1)],
                team2data.players[Random.nextInt(team2data.players.size - 1)]
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