package br.com.rossiny.topgames.api

import br.com.rossiny.topgames.BuildConfig
import br.com.rossiny.topgames.models.Result
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface GameService {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
        "Client-ID: " + BuildConfig.ClientId,
        "User-Agent: Retrofit-Sample-App")
    @GET("games/top")
    fun getTopRatedGames(
        @Query("offset") offset: Int
    ): Call<Result>

}
