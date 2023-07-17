package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import javax.inject.Inject


class LocalBookDataSource @Inject constructor(){
    private val bookList = mutableListOf<Book>(Book(Int.MAX_VALUE, "", "", ""))
    private val maxBookListLong = 20
    private var recentlyAddBookId = 0

    private fun binarySearch(target: Int): Int {
        return binarySearch(target, 0, bookList.size - 1)
    }

    private fun binarySearch(target: Int, minIndex: Int, maxIndex: Int): Int {
        if (minIndex > maxIndex) return -1
        if (target < bookList[minIndex].bookID) return minIndex
        if (target > bookList[maxIndex].bookID) return maxIndex + 1
        val mid = (minIndex + maxIndex) / 2
        return when {
            target == bookList[mid].bookID -> mid
            target > bookList[mid].bookID -> binarySearch(target, mid + 1, maxIndex)
            target < bookList[mid].bookID -> binarySearch(target, minIndex, mid - 1)
            else -> -1
        }
    }

    private fun remove(bookId: Int) {
        if (bookList.size == 0) {return}
        val index = binarySearch(bookId)
        if (bookList[index].bookID == bookId) {
            bookList.removeAt(index)
        }
    }

    fun getBook(bookId: Int):Book? {
        if (bookList.size == 0) {return null}
        var index = binarySearch(bookId)
        if (bookList[index].bookID == bookId) {
            return bookList[index]
        }
        return null
    }


    fun add(book: Book) {
        if (bookList.size == 0) {
            bookList.add(book)
            return
        }
        if (bookList.size >= maxBookListLong) {
            remove(book.bookID)
        }
        val index = binarySearch(book.bookID)
        recentlyAddBookId = book.bookID
        bookList.add(index, book)
    }


}