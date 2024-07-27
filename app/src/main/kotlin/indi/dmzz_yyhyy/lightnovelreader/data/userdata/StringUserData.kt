package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import kotlinx.coroutines.flow.Flow

class StringUserData (
    override val path: String,
    private val userDataDao: UserDataDao
) : UserData<String>(path) {
    override fun set(value: String) {
        userDataDao.update(path, group, "String", value)
    }

    override fun get(): String? {
        return userDataDao.get(path)
    }

    override fun getFlow(): Flow<String?> {
        return userDataDao.getFlow(path)
    }
}