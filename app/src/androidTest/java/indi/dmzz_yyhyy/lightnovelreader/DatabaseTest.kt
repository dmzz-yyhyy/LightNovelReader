package indi.dmzz_yyhyy.lightnovelreader

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.LightNovelReaderDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.location.TestBookData
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var bookInformationDao: BookInformationDao
    private lateinit var volumesDao: BookVolumesDao
    private lateinit var db: LightNovelReaderDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, LightNovelReaderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bookInformationDao = db.bookInformationDao()
        volumesDao = db.bookVolumesDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }



    @Test
    @Throws(Exception::class)
    fun insertAndGet() {

        CoroutineScope(Dispatchers.Default).launch {
            bookInformationDao.update(TestBookData.bookInformation)
            println(bookInformationDao.get(0))

            volumesDao.update(0, TestBookData.bookVolumes)
            println(volumesDao.getBookVolumes(0))
        }
    }
}