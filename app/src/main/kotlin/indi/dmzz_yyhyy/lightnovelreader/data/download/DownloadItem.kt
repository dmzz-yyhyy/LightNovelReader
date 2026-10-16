package indi.dmzz_yyhyy.lightnovelreader.data.download

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime

sealed interface DownloadStage {
    data object Idle : DownloadStage
    data class Chapters(val current: Int, val total: Int) : DownloadStage
    data class Images(val current: Int, val total: Int) : DownloadStage
    data object Saving : DownloadStage
}

interface DownloadItem {
    val type: DownloadType
    val bookId: String
    val startTime: LocalDateTime
    val progress: Float
    val stage: DownloadStage
    val detail: String
}

@Stable
class MutableDownloadItem(
    override val type: DownloadType,
    override val bookId: String,
    override val startTime: LocalDateTime = LocalDateTime.now()
): DownloadItem {
    override var progress by mutableFloatStateOf(0f)
    override var stage: DownloadStage by mutableStateOf(DownloadStage.Idle)
    override var detail by mutableStateOf("")

    override fun equals(other: Any?): Boolean {
        return if (other is DownloadItem) other.type == this.type && other.bookId == this.bookId else super.equals(other)
    }

    override fun toString(): String {
        return "MutableDownloadItem{type=$type, bookId=$bookId, startTimeMillis=$startTime, progress=$progress, stage=$stage, detail=$detail}"
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + bookId.hashCode()
        return result
    }
}
