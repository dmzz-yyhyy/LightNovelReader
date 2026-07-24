package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 整数类型的用户数据
 *
 * @param path 用户数据的完整路径字符串
 * @param userDataDao 底层数据访问接口
 *
 * @since Api 4
 */
class IntUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Int>(path) {
    override suspend fun set(value: Int) {
        userDataDao.insert(path, group, "Int", value.toString())
    }

    override suspend fun get(): Int? {
        return userDataDao.get(path)?.toInt()
    }

    override fun getFlow(): Flow<Int?> {
        return userDataDao.getFlow(path).map { it?.toInt() }
    }
}