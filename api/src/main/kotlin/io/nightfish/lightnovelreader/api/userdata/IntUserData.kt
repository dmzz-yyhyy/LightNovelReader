package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IntUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Int>(path) {
    override fun set(value: Int) {
        userDataDao.insert(path, group, "Int", value.toString())
    }

    override fun get(): Int? {
        return userDataDao.get(path)?.toInt()
    }

    override fun getFlow(): Flow<Int?> {
        return userDataDao.getFlow(path).map { it?.toInt() }
    }
}