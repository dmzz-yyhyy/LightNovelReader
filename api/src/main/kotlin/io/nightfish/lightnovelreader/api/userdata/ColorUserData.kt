package io.nightfish.lightnovelreader.api.userdata

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ColorUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Color>(path) {
    override fun set(value: Color) {
        userDataDao.insert(path, group, "Color", value.value.toString())
    }

    override fun get(): Color? {
        return userDataDao.get(path)?.toULong().let { Color(it?: return null) }
    }

    override fun getFlow(): Flow<Color?> {
        return userDataDao.getFlow(path).map { it?.toULong() }.map { Color(it?: return@map null) }
    }
}