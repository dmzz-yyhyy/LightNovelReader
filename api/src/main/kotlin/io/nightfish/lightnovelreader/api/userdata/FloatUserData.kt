package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FloatUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Float>(path) {
    override fun set(value: Float) {
        userDataDao.insert(path, group, "Float", value.toString())
    }

    override fun get(): Float? {
        return userDataDao.get(path)?.toFloat()
    }

    override fun getFlow(): Flow<Float?> {
        return userDataDao.getFlow(path).map { it?.toFloat() }
    }
}