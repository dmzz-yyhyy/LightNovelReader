package indi.dmzz_yyhyy.lightnovelreader.api

import indi.dmzz_yyhyy.lightnovelreader.data.book.*
import indi.dmzz_yyhyy.lightnovelreader.data.local.Config
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.SocketTimeoutException
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
                .header("Connection","close")
                .header("Connection","close")
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
        fun getBookInformation(@Query("book_id") bookID: Int): Call<WebData<Information>>

        @GET("get_book_chapter_list")
        fun getBookChapterList(@Query("book_id") bookID: Int): Call<WebData<List<Volume>>>

        @GET("get_book_chapter_content")
        fun getBookContent(@Query("book_id") bookId: Int, @Query("chapter_id") chapterId: Int): Call<WebData<ChapterContent>>
    }

    fun getBookInformation(bookId: Int, reconnectTimes: Int = 5): Information? {
        return try {
            val dataCall: Call<WebData<Information>> = service.getBookInformation(bookId)
            val data: Response<WebData<Information>>? = dataCall.execute()
            data?.body()?.data
        } catch (error: SocketTimeoutException){
            if (reconnectTimes != 0) { return getBookInformation(bookId, reconnectTimes - 1) }
            null
        }
    }

    fun getBookChapterList(bookId: Int, reconnectTimes: Int = 5): List<Volume>? {
        return try {
            val dataCall: Call<WebData<List<Volume>>> = service.getBookChapterList(bookId)
            val data: Response<WebData<List<Volume>>>? = dataCall.execute()
            data?.body()?.data
        } catch (error: SocketTimeoutException){
            if (reconnectTimes != 0) { return getBookChapterList(bookId, reconnectTimes - 1) }
            null
        }
    }

    fun getBookContent(bookId: Int, chapterId: Int, reconnectTimes: Int = 5): ChapterContent? {
        return try {
            val dataCall: Call<WebData<ChapterContent>> = service.getBookContent(bookId, chapterId)
            val data: Response<WebData<ChapterContent>>? = dataCall.execute()
            data?.body()?.data
        } catch (error: SocketTimeoutException){
            if (reconnectTimes != 0) { return getBookContent(bookId, chapterId, reconnectTimes - 1) }
            null
        }
    }

}