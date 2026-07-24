package io.nightfish.lightnovelreader.api.userdata

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * URI类型的用户数据
 *
 * @param path 用户数据的完整路径字符串
 * @param userDataDao 底层数据访问接口
 *
 * @since Api 4
 */
class UriUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Uri>(path) {
    override suspend fun set(value: Uri) {
        userDataDao.insert(path, group, "Uri", value.toString())
    }

    override suspend fun get(): Uri? {
        return userDataDao.get(path)?.toUri()
    }

    override fun getFlow(): Flow<Uri?> {
        return userDataDao.getFlow(path).map { it?.toUri() }
    }
}