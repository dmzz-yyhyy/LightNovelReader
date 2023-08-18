package indi.dmzz_yyhyy.lightnovelreader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import indi.dmzz_yyhyy.lightnovelreader.data.ReadingBookRepository
import javax.inject.Inject

@HiltAndroidApp
class LightNovelReaderApplication: Application(){
    @Inject
    lateinit var readingBookRepository: ReadingBookRepository

    override fun onCreate() {
        super.onCreate()
    }

}