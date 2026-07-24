package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow

/**
 * 字符串类型的用户数据
 *
 * @param path 用户数据的完整路径字符串
 * @param userDataDao 底层数据访问接口
 *
 * @since Api 4
 */
class StringUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<String>(path) {
    override suspend fun set(value: String) {
        userDataDao.insert(path, group, "String", value)
    }

    override suspend fun get(): String? {
        return userDataDao.get(path)
    }

    override fun getFlow(): Flow<String?> {
        return userDataDao.getFlow(path)
    }
}