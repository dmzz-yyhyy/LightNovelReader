package indi.dmzz_yyhyy.lightnovelreader.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import indi.dmzz_yyhyy.lightnovelreader.ui.reader.ReaderActivity

data class ReadingUiState(
    val readingBookDataList: List<Book> = listOf(),
    val onCardClick: (Int, Context) -> Unit = { bookId: Int, context: Context ->
        val intent = Intent(context, ReaderActivity::class.java)
        intent.putExtra("bookId", bookId)
        startActivity(context, intent, Bundle())
    }
)