package projekt.tf2liveticker.api_callers

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitEtf2lInstance {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-v2.etf2l.org/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val etf2l_api: Etf2l_API = retrofit.create(Etf2l_API::class.java)
}