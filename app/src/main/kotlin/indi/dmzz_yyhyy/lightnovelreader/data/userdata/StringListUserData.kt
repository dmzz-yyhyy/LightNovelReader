package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StringListUserData (
    override val path: String,
    private val userDataDao: UserDataDao
) : UserData<List<String>>(path) {
    override fun set(value: List<String>) {
        userDataDao.update(path, group, "StringList", value.joinToString(","))
    }

    override fun get(): List<String>? {
        return userDataDao.get(path)?.split(",")
    }

    override fun getFlow(): Flow<List<String>?> {
        return userDataDao.getFlow(path).map { it?.split(",") }
    }

    fun update(data: (List<String>) -> List<String>) {
        update(data, emptyList())
    }
}