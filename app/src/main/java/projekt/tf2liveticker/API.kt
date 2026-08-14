package projekt.tf2liveticker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.jvm.java

interface API {
    @GET("matches?scheduled=1")
    suspend fun getMatches(): ApiResponse //

    @GET("matches")
    suspend fun getSelectedMatches(
        @Query("id") id: Int
    ): ApiResponse
}

