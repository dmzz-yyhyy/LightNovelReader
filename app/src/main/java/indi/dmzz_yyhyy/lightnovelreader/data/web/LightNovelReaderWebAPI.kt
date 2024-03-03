package indi.dmzz_yyhyy.lightnovelreader.data.web

import android.util.Log
import indi.dmzz_yyhyy.lightnovelreader.data.local.Config
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class LightNovelReaderWebAPI @Inject constructor() {
    companion object {
        private val interceptor = Interceptor { chain ->
            val request = chain.request()
            val requestBuilder = request.newBuilder()
            val url = request.url
            val builder = url.newBuilder()
            requestBuilder.url(builder.build())
                .method(request.method, request.body)
                .header("Connection", "close")
                .header("Connection", "close")
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
        fun getBookInformation(@Query("book_id") bookID: Int): Call<Data<Information>>

        @GET("get_book_chapter_list")
        fun getBookChapterList(@Query("book_id") bookID: Int): Call<Data<List<Volume>>>

        @GET("get_book_chapter_content")
        fun getBookContent(
            @Query("book_id") bookId: Int,
            @Query("chapter_id") chapterId: Int,
        ): Call<Data<ChapterContent>>

        @GET("search_book")
        fun searchBook(
            @Query("search_type") searchType: String,
            @Query("keyword") keyword: String,
            @Query("page") page: Int,
        ): Call<Data<SearchBooks>>

        @GET(".")
        fun getServerMetadata(): Call<ServerMetadata>

        @GET("update_apk")
        fun getUpdateApk(): Call<ResponseBody>
    }


    fun getBookInformation(bookId: Int, reconnectTimes: Int = 5): Information? {
        return try {
            Log.d("API", "Attempting to getBookInformation: bookID=$bookId. [remaining $reconnectTimes]")
            val dataCall: Call<Data<Information>> = service.getBookInformation(bookId)
            val data: Response<Data<Information>>? = dataCall.execute()
            Log.d("API", data.toString())
            data?.body()?.data
        } catch (error: Exception) {
            if (reconnectTimes > 0) {
                return getBookInformation(bookId, reconnectTimes - 1)
            }
            null
        }

    }

    fun getBookChapterList(bookId: Int, reconnectTimes: Int = 5): List<Volume>? {
        return try {
            Log.d("API", "Attempting to getBookChapterList: bookID=$bookId. [remaining $reconnectTimes]")
            val dataCall: Call<Data<List<Volume>>> = service.getBookChapterList(bookId)
            val data: Response<Data<List<Volume>>>? = dataCall.execute()
            data?.body()?.data
        } catch (error: Exception) {
            if (reconnectTimes != 0) {
                return getBookChapterList(bookId, reconnectTimes - 1)
            }
            null
        }
    }

    fun getBookContent(bookId: Int, chapterId: Int, reconnectTimes: Int = 5): ChapterContent? {
        return try {
            Log.d("API", "Attempting to getBookContent: bookID=$bookId. [remaining $reconnectTimes]")
            val dataCall: Call<Data<ChapterContent>> = service.getBookContent(bookId, chapterId)
            val data: Response<Data<ChapterContent>>? = dataCall.execute()
            data?.body()?.data
        } catch (error: Exception) {
            if (reconnectTimes != 0) {
                return getBookContent(bookId, chapterId, reconnectTimes - 1)
            }
            null
        }
    }

    fun searchBook(searchType: String, keyword: String, page: Int, reconnectTimes: Int = 5): SearchBooks? {
        return try {
            Log.d(
                "API",
                "Attempting to searchBook: searchType=$searchType, keyword=$keyword, page=$page. [remaining $reconnectTimes]"
            )
            val dataCall: Call<Data<SearchBooks>> = service.searchBook(searchType, keyword, page)
            val data: Response<Data<SearchBooks>>? = dataCall.execute()
            data?.body()?.data
        } catch (error: Exception) {
            error.localizedMessage?.let { Log.e("API", it) }
            if (reconnectTimes != 0) {
                return searchBook(searchType, keyword, page, reconnectTimes - 1)
            }
            null
        }
    }

    fun getServerMetadata(reconnectTimes: Int = 5): ServerMetadata? {
        return try {
            Log.d("API", "Attempting to getServerMetadata. [remaining $reconnectTimes]")
            val dataCall: Call<ServerMetadata> = service.getServerMetadata()
            val data: Response<ServerMetadata>? = dataCall.execute()
            data?.body()
        } catch (error: Exception) {
            error.localizedMessage?.let { Log.e("API", it) }
            if (reconnectTimes != 0) {
                return getServerMetadata(reconnectTimes - 1)
            }
            null
        }
    }

    fun getUpdateApk(reconnectTimes: Int = 5): ResponseBody? {
        return try {
            Log.d("API", "Attempting to getUpdateApk. [remaining $reconnectTimes]")
            val dataCall: Call<ResponseBody> = service.getUpdateApk()

            val data: Response<ResponseBody>? = dataCall.execute()
            Log.w("API", "老子tm下好了！")
            data?.body()
        } catch (error: Exception) {
            error.localizedMessage?.let { Log.e("API", it) }
            if (reconnectTimes != 0) {
                return getUpdateApk(reconnectTimes - 1)
            }
            null
        }
    }
}