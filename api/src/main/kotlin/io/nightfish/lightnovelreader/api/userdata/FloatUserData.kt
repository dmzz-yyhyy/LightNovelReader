package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 浮点数类型的用户数据
 *
 * @param path 用户数据的完整路径字符串
 * @param userDataDao 底层数据访问接口
 *
 * @since Api 4
 */
class FloatUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Float>(path) {
    override suspend fun set(value: Float) {
        userDataDao.insert(path, group, "Float", value.toString())
    }

    override suspend fun get(): Float? {
        return userDataDao.get(path)?.toFloat()
    }

    override fun getFlow(): Flow<Float?> {
        return userDataDao.getFlow(path).map { it?.toFloat() }
    }
}