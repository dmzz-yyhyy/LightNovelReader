package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.book.Book

object BookData {
    private val bookDataMap = hashMapOf<String, Book>()
    private var readingBookList: MutableList<Book> = mutableListOf()
    fun getBook(uid: String): Book? {
        return bookDataMap[uid]
    }
    fun addBook(book: Book) {
        bookDataMap[book.bookUID] = book
    }
    fun addReadingBook(book: Book) {
        readingBookList.add(book)
    }
    fun setReadingBookList(newReadingBookList: MutableList<Book>) {
        readingBookList = newReadingBookList
    }
    fun getReadingBookList(): List<Book> {
        return readingBookList
    }
}