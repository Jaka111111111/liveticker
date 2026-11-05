package projekt.tf2liveticker

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Log_API {

    @GET("log")
    suspend fun getRecentLogs(
        @Query("player") id: String?,
        @Query("limit") limit: Int = 1
    ): RecentLogResponse

    @GET("log/{id}")
    suspend fun getResults(@Path("id") id: Int): LogResponse
}