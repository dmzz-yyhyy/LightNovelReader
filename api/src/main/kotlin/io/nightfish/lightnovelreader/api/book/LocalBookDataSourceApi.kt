package io.nightfish.lightnovelreader.api.book

/**
 * 本地书本数据源接口
 * 提供对本地存储的书本数据的增删改查操作
 *
 * @since Api 2
 */
interface LocalBookDataSourceApi {
    /**
     * 通过书本id获取本地存储的书本详情
     *
     * @param id 书本id
     *
     * @return 书本详情, 如果本地不存在则返回null
     *
     * @since Api 2
     */
    suspend fun getBookInformation(id: String): BookInformation?

    /**
     * 更新或写入本地书本详情
     *
     * @param info 需要写入的书本详情
     *
     * @since Api 4
     */
    suspend fun updateBookInformation(info: BookInformation)

    /**
     * 通过书本id获取本地存储的书本卷目录
     *
     * @param id 书本id
     *
     * @return 书本卷目录, 如果本地不存在则返回null
     *
     * @since Api 2
     */
    suspend fun getBookVolumes(id: String): BookVolumes?

    /**
     * 更新或写入本地书本卷目录
     *
     * @param bookVolumes 需要写入的书本卷目录
     *
     * @since Api 4
     */
    suspend fun updateBookVolumes(bookVolumes: BookVolumes)

    /**
     * 通过章节id获取本地存储的章节内容
     *
     * @param id 章节id
     *
     * @return 可变的章节内容对象, 如果本地不存在则返回null
     *
     * @since Api 4
     */
    suspend fun getChapterContent(id: String): ChapterContent?

    /**
     * 更新或写入本地章节内容
     *
     * @param chapterContent 需要写入的章节内容
     *
     * @since Api 4
     */
    suspend fun updateChapterContent(chapterContent: ChapterContent)

    /**
     * 获取用户书本阅读数据
     *
     * @param id 书本id
     *
     * @return 可变的用户阅读数据对象
     *
     * @since Api 4
     */
    suspend fun getUserReadingData(id: String): UserReadingData

    /**
     * 更新用户书本阅读数据
     *
     * @param id 书本id
     * @param update 接收当前阅读数据并返回更新后数据的函数
     *
     * @since Api 4
     */
    suspend fun updateUserReadingData(id: String, update: (UserReadingData) -> UserReadingData)

    /**
     * 获取全部用户书本阅读数据
     *
     * @return 用户阅读数据的列表
     *
     * @since Api 4
     */
    suspend fun getAllUserReadingData(): List<UserReadingData>

    /**
     * 判断指定章节内容是否在本地已缓存
     *
     * @param id 章节id
     *
     * @return 章节内容是否存在于本地缓存
     *
     * @since Api 2
     */
    suspend fun isChapterContentExists(id: String): Boolean

    /**
     * 清空所有本地书本数据
     *
     * @since Api 2
     */
    suspend fun clear()
}