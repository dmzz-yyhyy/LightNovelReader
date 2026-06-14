package io.nightfish.lightnovelreader.api.bookshelf

/**
 * 书架排序方式
 *
 * @param key 排序方式的内部标识字符串
 *
 * @since Api 2
 */
enum class BookshelfSortType(val key: String) {
    /** 默认排序(添加的逆序) */
    Default("default"),
    /** 按最新更新时间排序 */
    Latest("latest"),
    /** 按名称排序 */
    Name("name"),
    /** 按字数排序 */
    WordCount("word_count");

    /** [BookshelfSortType]的工厂方法集合 */
    companion object {
        /**
         * 通过key字符串映射到对应的[BookshelfSortType]
         *
         * @param key 排序方式的key字符串
         *
         * @return 对应的[BookshelfSortType]
         *
         * @since Api 2
         */
        fun map(key: String): BookshelfSortType = entries.firstOrNull { it.key == key } ?: Default
    }
}
