package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import dagger.Lazy
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserData
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepository @Inject constructor(
    private val dao: UserDataDao,
    private val sourceProvider: Lazy<WebBookDataSourceProvider>
) : UserDataRepositoryApi {
    override fun <T> userData(path: UserDataPath<T>, source: Identifier.DataSource?): UserData<T> =
        UserData(
            path,
            path.identifier(
                source ?: if (path.sourceScoped) sourceProvider.get().value.id else null
            ),
            dao
        )

    override suspend fun remove(path: UserDataPath<*>, source: Identifier.DataSource?) =
        dao.remove(
            path.identifier(
                source ?: if (path.sourceScoped) sourceProvider.get().value.id else null
            )
        )
}
