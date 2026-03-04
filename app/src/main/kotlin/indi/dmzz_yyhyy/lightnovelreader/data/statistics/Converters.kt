package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity

fun BookRecordEntity.toData(): BookRecord =
    BookRecord(
        bookId = bookId,
        date = date,
        reads = reads,
        seconds = seconds,
        isFinished = isFinished,
        isFavorited = isFavorited,
        firstSeen = firstSeen,
        lastSeen = lastSeen,
    )

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