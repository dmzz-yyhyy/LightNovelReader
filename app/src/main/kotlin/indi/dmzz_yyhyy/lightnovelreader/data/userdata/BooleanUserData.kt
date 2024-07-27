package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao

class BooleanUserData (
    override val path: String,
    private val userDataDao: UserDataDao
) : UserData<Boolean>(path) {
    override fun set(value: Boolean) {
        userDataDao.update(path, group, "Float", value.toString())
    }

    override fun get(): Boolean? {
        return if (userDataDao.get(path) != null) userDataDao.get(path) == "true" else null
    }
}