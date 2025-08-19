package indi.dmzz_yyhyy.lightnovelreader.data.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfSortType
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class AppUserDataJson(
    @SerializedName("type")
    val type: String,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("data")
    val data: List<AppUserDataContent>
) {
    companion object {
        val gson: Gson = GsonBuilder()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter)
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter)
            .registerTypeAdapter(LocalTime::class.java, LocalTimeTypeAdapter)
            .registerTypeAdapter(BookshelfSortType::class.java, BookshelfSortTypeTypeAdapter)
            .registerTypeAdapter(Count::class.java, CountBase64TypeAdapter)
            .create()

        fun fromJson(json: String): AppUserDataJson =
            gson.fromJson(json, AppUserDataJson::class.java)
    }

    fun toJson(): String = gson.toJson(this)
}

data class AppUserDataContent(
    @SerializedName("web_data_source_id")
    val webDataSourceId: Int,
    @SerializedName("book_user_data")
    val bookUserData: List<BookUserData>? = null,
    @SerializedName("book_shelf")
    val bookshelf: List<BookshelfData>? = null,
    @SerializedName("book_shelf_book_metadata")
    val bookShelfBookMetadata: List<BookShelfBookMetadataData>? = null,
    @SerializedName("user_data")
    val userData: List<UserDataData>? = null,
    @SerializedName("reading_stats_data")
    val readingStatsData: List<DailyReadingStats>? = null,
    @SerializedName("format_data")
    val formattingRuleData: List<FormattingRuleData>? = null
)

class AppUserDataJsonBuilder {
    @SerializedName("id")
    private var id: Int? = null

    @SerializedName("data")
    private var data: MutableList<AppUserDataContent> = mutableListOf()

    fun build(): AppUserDataJson = AppUserDataJson(
        type = "light novel reader data file",
        id = id,
        data = data
    )

    fun data(data: AppUserDataContentBuilder.() -> Unit): AppUserDataJsonBuilder {
        this.data.add(
            AppUserDataContentBuilder()
                .let {
                    data.invoke(it)
                    it.build()
                }
        )
        return this
    }
}

class AppUserDataContentBuilder {
    private var webDataSourceId: Int? = null
    private var bookUserData: MutableList<BookUserData> = mutableListOf()
    private var bookshelf: MutableList<BookshelfData> = mutableListOf()
    private var bookShelfBookMetadata: MutableList<BookShelfBookMetadataData> = mutableListOf()
    private var userData: MutableList<UserDataData> = mutableListOf()
    private var readingStatsData: MutableList<DailyReadingStats> = mutableListOf()
    private var formattingRuleData: MutableList<FormattingRuleData> = mutableListOf()

    fun build(): AppUserDataContent {
        if (webDataSourceId == null) {
            throw NullPointerException("webDataSourceId can not be null")
        }
        return AppUserDataContent(
            webDataSourceId = webDataSourceId!!,
            bookUserData = bookUserData.ifEmpty { null },
            bookshelf = bookshelf.ifEmpty { null },
            bookShelfBookMetadata = bookShelfBookMetadata.ifEmpty { null },
            userData = userData.ifEmpty { null },
            readingStatsData = readingStatsData.ifEmpty { null },
            formattingRuleData = formattingRuleData.ifEmpty { null }
        )
    }

    fun webDataSourceId(webDataSourceId: Int): AppUserDataContentBuilder {
        this.webDataSourceId = webDataSourceId
        return this
    }

    fun bookUserData(bookUserData: BookUserData): AppUserDataContentBuilder {
        this.bookUserData.add(bookUserData)
        return this
    }

    fun bookshelf(bookshelf: BookshelfData): AppUserDataContentBuilder {
        this.bookshelf.add(bookshelf)
        return this
    }

    fun bookshelfBookMetaData(bookshelfBookMetadata: BookShelfBookMetadataData): AppUserDataContentBuilder {
        this.bookShelfBookMetadata.add(bookshelfBookMetadata)
        return this
    }

    fun userData(userData: UserDataData): AppUserDataContentBuilder {
        this.userData.add(userData)
        return this
    }

    fun dailyReadingData(dailyReadingStats: DailyReadingStats): AppUserDataContentBuilder {
        this.readingStatsData.add(dailyReadingStats)
        return this
    }

    fun formattingRule(formattingRuleData: FormattingRuleData): AppUserDataContentBuilder {
        this.formattingRuleData.add(formattingRuleData)
        return this
    }
}