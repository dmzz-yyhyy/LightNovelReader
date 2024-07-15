package indi.dmzz_yyhyy.lightnovelreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import indi.dmzz_yyhyy.lightnovelreader.ui.LightNovelReaderApp
import indi.dmzz_yyhyy.lightnovelreader.ui.theme.LightNovelReaderTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LightNovelReaderTheme {
                LightNovelReaderApp()
            }
        }
    }
}