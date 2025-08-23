package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import kotlinx.serialization.Serializable

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
                val selectedBookIds: List<Int>
            )
        }
        @Serializable
        object Exploration {
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
                data class Rules(val bookId: Int)
            }
            @Serializable
            object Debug
            @Serializable
            object Theme
        }
        @Serializable
        object Extensions {
            @Serializable
            object Home
            @Serializable
            object Repositories
            @Serializable
            object Browse
            @Serializable
            object Installed
        }
        @Serializable
        object SourceChangeDialog
        @Serializable
        object ExportUserDataDialog
        @Serializable
        data class EditTextFormattingRuleDialog(
            val bookId: Int,
            val ruleId: Int
        )
    }
    @Serializable
    object Book {
        @Serializable
        data class Detail(
            val bookId: Int
        )
        @Serializable
        data object Reader
        @Serializable
        data class ExportUserDataDialog(
            val bookId: Int,
            val title: String
        )
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
        data class ImageViewerDialog(
            val imageUrl: String
        )

    }
    @Serializable
    object UpdatesAvailableDialog
    @Serializable
    data class AddBookToBookshelfDialog(
        val bookId: Int
    )
    @Serializable
    data class MarkAllChaptersAsReadDialog(
        val bookId: Int
    )
    @Serializable
    data class SliderValueDialog(
        val value: Float,
        val floatUserDataPath: String
    )
    @Serializable
    object DownloadManager
}