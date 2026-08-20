package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.LightNovelReaderDatabase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugScreenViewModel @Inject constructor(
    val database: LightNovelReaderDatabase
) : ViewModel() {
    var result by mutableStateOf("")
        private set

    @SuppressLint("Range")
    fun runSQLCommand(command: String) {
        viewModelScope.launch {
            val db = database.openHelper.writableDatabase
            val cursor = db.query(command)
            if (cursor.moveToFirst()) {
                do {
                    result += cursor.columnNames.map {
                        it + ": " + cursor.getString(cursor.getColumnIndex(it))
                    } + "\n"
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
        }


    }
}