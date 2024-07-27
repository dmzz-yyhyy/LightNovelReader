package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao

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
}