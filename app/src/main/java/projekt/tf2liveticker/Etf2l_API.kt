package projekt.tf2liveticker

import projekt.tf2liveticker.Models.LogResponse
import projekt.tf2liveticker.Models.MatchesResponse
import projekt.tf2liveticker.Models.RecentLogResponse
import projekt.tf2liveticker.Models.TeamResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Etf2l_API {
    @GET("matches?scheduled=1")
    suspend fun getMatches(): MatchesResponse //


    @GET("team/{id}")
    suspend fun getTeam(@Path("id") id: Int): TeamResponse //
}

interface Log_API {

    @GET("log")
    suspend fun getRecentLogs(
        @Query("player") id: String?,
        @Query("limit") limit: Int = 2
    ): RecentLogResponse

    @GET("log/{id}")
    suspend fun getResults(@Path("id") id: Int): LogResponse
}