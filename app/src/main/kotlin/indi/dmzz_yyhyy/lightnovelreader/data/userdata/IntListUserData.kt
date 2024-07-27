package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao

class IntListUserData (
    override val path: String,
    private val userDataDao: UserDataDao
) : UserData<List<Int>>(path) {
    override fun set(value: List<Int>) {
        userDataDao.update(path, group, "IntList", value.joinToString(","))
    }

    override fun get(): List<Int>? {
        return userDataDao.get(path)?.split(",")?.map(String::toInt)
    }

    fun update(data: (List<Int>) -> List<Int>) {
        update(data, emptyList())
    }
}