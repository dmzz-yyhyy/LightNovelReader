package io.nightfish.lightnovelreader.api.userdata

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UriUserData (
    override val path: String,
    private val userDataDao: UserDataDaoApi
) : UserData<Uri>(path) {
    override fun set(value: Uri) {
        userDataDao.insert(path, group, "Uri", value.toString())
    }

    override fun get(): Uri? {
        return userDataDao.get(path)?.toUri()
    }

    override fun getFlow(): Flow<Uri?> {
        return userDataDao.getFlow(path).map { it?.toUri() }
    }
}