package logic

import android.util.Log
import com.filisamp.acccriticaltechworks.BuildConfig
import interfaces.NewsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.NewsItemModel
import models.SourcesResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitHelper {

    object NewsApiClient {
        private const val BASE_URL = "https://newsapi.org/v2/"
        private const val API_KEY = BuildConfig.NEWS_API_KEY

        private fun createService(): NewsApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(NewsApiService::class.java)
        }

        suspend fun fetchSourcesAsync(): SourcesResponse? = withContext(Dispatchers.IO) {
            try {
                val sourcesService = createService().getSources(API_KEY)
                val response = sourcesService.execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                // Log any exceptions that might occur during the API call
                Log.e("API_CALL::fetchSourcesAsync", "Exception: ${e.message}")
                null
            }
        }

        suspend fun fetchNewsForSourceAsync(tmpSource: String): NewsItemModel? = withContext(Dispatchers.IO) {
            try {
                val newsService = createService().getTopHeadlinesBySource(tmpSource, API_KEY)
                val response = newsService.execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                // Log any exceptions that might occur during the API call
                Log.e("API_CALL::fetchNewsForSourceAsync", "Exception: ${e.message}")
                null
            }
        }

        suspend fun fetchNewsForCountryAsync(tmpCountry: String): NewsItemModel? = withContext(Dispatchers.IO) {
            val newsService = createService().getTopHeadlinesByCountry(tmpCountry,API_KEY)
            val response = newsService.execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }

        suspend fun fetchNewsForAsync(tmpText: String): NewsItemModel? = withContext(Dispatchers.IO) {
            val newsService = createService().getEverything(tmpText,API_KEY)
            val response = newsService.execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }

    }

}