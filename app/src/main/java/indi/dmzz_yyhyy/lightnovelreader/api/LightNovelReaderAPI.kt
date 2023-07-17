package indi.dmzz_yyhyy.lightnovelreader.api

import indi.dmzz_yyhyy.lightnovelreader.data.book.Information
import indi.dmzz_yyhyy.lightnovelreader.data.local.Config
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class LightNovelReaderAPI @Inject constructor() {
    companion object {
        private val interceptor = Interceptor { chain ->
            val request = chain.request()
            val requestBuilder = request.newBuilder()
            val url = request.url
            val builder = url.newBuilder()
            requestBuilder.url(builder.build())
                .method(request.method, request.body)
                .addHeader("clientType", "IOS")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
            chain.proceed(requestBuilder.build())
        }
        private val client: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)


        private var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Config.backgroundServerURL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()

        var service: LightNovelReaderBackgroundAPI = retrofit.create(LightNovelReaderBackgroundAPI::class.java)


    }

    // Makes news-related network synchronous requests.
    interface LightNovelReaderBackgroundAPI {
        @GET("get_book_information")
        fun getBookInformation(@Query("book_id") bookID: Int): Call<Information>
    }

        fun getBookInformation(bookID: Int): Information? {
            val dataCall: Call<Information> = service.getBookInformation(bookID)
            val data: Response<Information>? = dataCall.execute()
            return data?.body()
        }

}