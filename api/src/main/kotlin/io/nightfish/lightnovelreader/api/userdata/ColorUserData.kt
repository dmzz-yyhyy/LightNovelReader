package io.nightfish.lightnovelreader.api.userdata

import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 颜色类型的用户数据
 *
 * @param path 用户数据的完整路径字符串
 * @param userDataDao 底层数据访问接口
 *
 * @since Api 2
 */
class ColorUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Color>(path) {
    override fun set(value: Color) {
        userDataDao.insert(path, group, "Color", value.value.toString())
    }

    override fun get(): Color? {
        return try {
            userDataDao.get(path)?.toULong().let { Color(it?: return null) }
        } catch (e: NumberFormatException) {
            Log.e("ColorUserData", "Failed to parse color value at path: $path", e)
            null
        }
    }

    override fun getFlow(): Flow<Color?> {
        return userDataDao.getFlow(path).map { rawValue ->
            try {
                rawValue?.toULong().let { Color(it?: return@map null) }
            } catch (e: NumberFormatException) {
                Log.e("ColorUserData", "Failed to parse color value at path: $path", e)
                null
            }
        }
    }
}