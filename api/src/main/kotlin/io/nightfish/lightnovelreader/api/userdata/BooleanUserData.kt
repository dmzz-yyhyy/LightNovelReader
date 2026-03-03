package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BooleanUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Boolean>(path) {
    override fun set(value: Boolean) {
        userDataDao.insert(path, group, "Float", value.toString())
    }

    override fun get(): Boolean? {
        return if (userDataDao.get(path) != null) userDataDao.get(path) == "true" else null
    }

    override fun getFlow(): Flow<Boolean?> {
        return userDataDao.getFlow(path).map { if (it != null) it == "true" else null }
    }
}