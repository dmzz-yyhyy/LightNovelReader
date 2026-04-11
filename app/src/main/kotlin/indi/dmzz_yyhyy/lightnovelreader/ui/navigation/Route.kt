package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
object Route {
    @Serializable
    object Main {
        @Serializable
        object Reading {
            @Serializable
            object Home
            @Serializable
            object Stats {
                @Serializable
                object Overview
                @Serializable
                data class Detailed(val targetDate: Int)
            }
        }
        @Serializable
        object Bookshelf {
            @Serializable
            object Home
            @Serializable
            data class Edit(
                val id: Int,
                val title: String
            )
            @Serializable
            data class DeleteBookshelfDialog(
                val bookshelfId: Int
            )
            @Serializable
            data class AddBookToBookshelfDialog(
                val selectedBookIds: List<String>
            )
        }
        @Serializable
        object Explore {
            @Serializable
            object Home
            @Serializable
            object Search
            @Serializable
            data class Expanded(
                val expandedPageDataSourceId: String
            )
        }
        @Serializable
        object Settings {
            @Serializable
            object Home
            @Serializable
            object Logcat
            @Serializable
            object TextFormatting {
                @Serializable
                object Manager
                @Serializable
                data class Rules(val bookId: String)
            }
            @Serializable
            object PluginManager {
                @Serializable
                object Home
                @Serializable
                data class Detail(
                    val id: String
                )
                @Serializable
                object AppList
                @Serializable
                object Repository
            }
            @Serializable
            object SourceChange {
                @Serializable
                object List
                @Serializable
                data class Settings(
                    val sourceId: String
                )
            }
            @Serializable
            object Debug
            @Serializable
            object Theme {
                @Serializable
                object Main
                @Serializable
                object CostumeThemeScheme
            }
            @Serializable
            object Licenses
            @Serializable
            object Formats
        }
        @Serializable
        object ExportUserDataDialog
        @Serializable
        data class EditTextFormattingRuleDialog(
            val bookId: String,
            val ruleId: Int
        )
    }
    @Serializable
    object Book {
        @Serializable
        data class Detail(
            val bookId: String
        )
        @Serializable
        data object Reader

        @Serializable
        data class ColorPickerDialog(
            val colorUserDataPath: String,
            val colors: LongArray
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ColorPickerDialog

                if (colorUserDataPath != other.colorUserDataPath) return false
                if (!colors.contentEquals(other.colors)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = colorUserDataPath.hashCode()
                result = 31 * result + colors.contentHashCode()
                return result
            }
        }
        @Serializable
        data class CostumeColorPickerDialog(
            val colorUserDataPath: String,
            val colors: LongArray
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as CostumeColorPickerDialog

                if (colorUserDataPath != other.colorUserDataPath) return false
                if (!colors.contentEquals(other.colors)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = colorUserDataPath.hashCode()
                result = 31 * result + colors.contentHashCode()
                return result
            }
        }
        @Serializable
        data class ImageViewerDialog(
            val imageUri: String
        )
    }
    @Serializable
    object UpdatesAvailableDialog
    @Serializable
    data class AddBookToBookshelfDialog(
        val bookId: String
    )
    @Serializable
    data class MarkAllChaptersAsReadDialog(
        val bookId: String
    )
    @Serializable
    data class SliderValueDialog(
        val value: Float,
        val floatUserDataPath: String
    )
    @Serializable
    data class PluginInstallerDialog(
        val source: String
    )
    @Serializable
    object DownloadManager
}
