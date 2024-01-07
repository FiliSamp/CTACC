package interfaces

import models.NewsItemModel
import models.SourcesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top-headlines")
    fun getTopHeadlinesByCountry(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsItemModel>

    @GET("top-headlines")
    fun getTopHeadlinesBySource(
        @Query("sources") country: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsItemModel>

    @GET("top-headlines/sources")
    fun getSources(
        @Query("apiKey") apiKey: String
    ): Call<SourcesResponse>

    @GET("everything")
    fun getEverything(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsItemModel>

}