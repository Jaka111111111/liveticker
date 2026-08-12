package projekt.tf2liveticker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-v2.etf2l.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: API = retrofit.create(API::class.java)
}
