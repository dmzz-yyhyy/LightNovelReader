package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 布尔类型的用户数据
 *
 * @param path 用户数据的完整路径字符串
 * @param userDataDao 底层数据访问接口
 *
 * @since Api 4
 */
class BooleanUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Boolean>(path) {
    override suspend fun set(value: Boolean) {
        userDataDao.insert(path, group, "Float", value.toString())
    }

    override suspend fun get(): Boolean? {
        return if (userDataDao.get(path) != null) userDataDao.get(path) == "true" else null
    }

    override fun getFlow(): Flow<Boolean?> {
        return userDataDao.getFlow(path).map { if (it != null) it == "true" else null }
    }
}