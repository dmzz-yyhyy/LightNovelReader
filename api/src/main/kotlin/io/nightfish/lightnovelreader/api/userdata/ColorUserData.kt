package io.nightfish.lightnovelreader.api.userdata

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 颜色类型的用户数据
 *
 * @param path 用户数据的完整路径字符串
 * @param userDataDao 底层数据访问接口
 *
 * @since Api 4
 */
class ColorUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Color>(path) {
    override suspend fun set(value: Color) {
        userDataDao.insert(path, group, "Color", value.value.toString())
    }

    override suspend fun get(): Color? {
        return userDataDao.get(path)?.toULong().let { Color(it?: return null) }
    }

    override fun getFlow(): Flow<Color?> {
        return userDataDao.getFlow(path).map { it?.toULong() }.map { Color(it?: return@map null) }
    }
}