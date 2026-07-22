package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 整数列表类型的用户数据
 * 内部以英文逗号分隔字符串的形式存储
 *
 * @param path 用户数据的完整路径字符串
 * @param userDataDao 底层数据访问接口
 *
 * @since Api 2
 */
class IntListUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<List<Int>>(path) {
    override suspend fun set(value: List<Int>) {
        userDataDao.insert(path, group, "IntList", value.joinToString(","))
    }

    override suspend fun get(): List<Int>? {
        return userDataDao.get(path)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map(String::toInt)
    }

    override fun getFlow(): Flow<List<Int>?> {
        return userDataDao.getFlow(path).map { text ->
            text?.split(",")
                ?.filter { it.isNotBlank() }
                ?.map(String::toInt)
        }
    }

    /**
     * 以变换函数更新当前列表数据
     *
     * @param data 接收旧列表并返回新列表的函数
     *
     * @since Api 4
     */
    suspend fun update(data: (List<Int>) -> List<Int>) {
        update(data, emptyList())
    }
}