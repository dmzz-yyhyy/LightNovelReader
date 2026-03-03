package io.nightfish.lightnovelreader.api.userdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IntListUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<List<Int>>(path) {
    override fun set(value: List<Int>) {
        userDataDao.insert(path, group, "IntList", value.joinToString(","))
    }

    override fun get(): List<Int>? {
        return userDataDao.get(path)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map(String::toInt)
    }

    override fun getFlow(): Flow<List<Int>?> {
        return userDataDao.getFlow(path).map { text ->
            text?.split(",")
                ?.filter { it.isNotBlank() }
                ?.map(String::toInt)
        }
    }

    fun update(data: (List<Int>) -> List<Int>) {
        update(data, emptyList())
    }
}