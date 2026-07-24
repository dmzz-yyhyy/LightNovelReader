package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 字符串列表类型的用户数据
 * 内部以英文逗号分隔字符串的形式存储
 *
 * @param path 用户数据的完整路径字符串
 * @param userDataDao 底层数据访问接口
 *
 * @since Api 4
 */
class StringListUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<List<String>>(path) {
    override suspend fun set(value: List<String>) {
        userDataDao.insert(path, group, "StringList", value.joinToString(","))
    }

    override suspend fun get(): List<String>? {
        return userDataDao.get(path)?.split(",")
    }

    override fun getFlow(): Flow<List<String>?> {
        return userDataDao.getFlow(path).map { it?.split(",") }
    }

    /**
     * 以变换函数更新当前列表数据
     *
     * @param data 接收旧列表并返回新列表的函数
     *
     * @since Api 4
     */
    suspend fun update(data: (List<String>) -> List<String>) {
        update(data, emptyList())
    }
}