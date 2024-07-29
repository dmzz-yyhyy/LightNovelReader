package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.BooleanUserData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.FloatUserData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.IntListUserData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.StringListUserData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.StringUserData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepository @Inject constructor(
    private val userDataDao: UserDataDao
) {
    fun stringUserData(path: String): StringUserData = StringUserData(path, userDataDao)
    fun floatUserData(path: String): FloatUserData = FloatUserData(path, userDataDao)
    fun booleanUserData(path: String): BooleanUserData = BooleanUserData(path, userDataDao)
    fun intListUserData(path: String): IntListUserData = IntListUserData(path, userDataDao)
    fun stringListUserData(path: String): StringListUserData = StringListUserData(path, userDataDao)
}