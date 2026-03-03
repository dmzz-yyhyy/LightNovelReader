package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow

class StringUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<String>(path) {
    override fun set(value: String) {
        userDataDao.insert(path, group, "String", value)
    }

    override fun get(): String? {
        return userDataDao.get(path)
    }

    override fun getFlow(): Flow<String?> {
        return userDataDao.getFlow(path)
    }
}