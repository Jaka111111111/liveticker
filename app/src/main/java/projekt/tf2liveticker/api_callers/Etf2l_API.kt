package projekt.tf2liveticker.api_callers

import projekt.tf2liveticker.MatchesResponse
import projekt.tf2liveticker.TeamResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Etf2l_API {
    @GET("matches?scheduled=1?type=6v6")
    suspend fun getMatches(@Query("page") page: Int
    ): MatchesResponse //


    @GET("team/{id}")
    suspend fun getTeam(@Path("id") id: Int): TeamResponse //
}

