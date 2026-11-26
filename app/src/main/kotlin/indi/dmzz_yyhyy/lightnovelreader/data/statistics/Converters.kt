package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow

fun BookRecordEntity.toData(
    bookRepository: BookRepository,
    bookInformationFlow: Flow<Result<BookInformation, WebRequestError>> = bookRepository.getBookInformationFlow(bookId)
): BookRecord =
    BookRecord(
        bookId = bookId,
        bookInformationFlow = bookInformationFlow,
        date = date,
        reads = reads,
        seconds = seconds,
        isFinished = isFinished,
        isFavorited = isFavorited,
        firstSeen = firstSeen,
        lastSeen = lastSeen,
    )

@Suppress("unused")
fun BookRecord.toEntity(): BookRecordEntity =
    BookRecordEntity(
        bookId = bookId,
        date = date,
        reads = reads,
        seconds = seconds,
        isFinished = isFinished,
        isFavorited = isFavorited,
        firstSeen = firstSeen,
        lastSeen = lastSeen,
    )
