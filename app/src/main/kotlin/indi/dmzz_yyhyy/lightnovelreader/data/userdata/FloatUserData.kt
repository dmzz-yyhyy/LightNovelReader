package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao

class FloatUserData (
    override val path: String,
    private val userDataDao: UserDataDao
) : UserData<Float>(path) {
    override fun set(value: Float) {
        userDataDao.update(path, group, "Float", value.toString())
    }

    override fun get(): Float? {
        return userDataDao.get(path)?.toFloat()
    }
}