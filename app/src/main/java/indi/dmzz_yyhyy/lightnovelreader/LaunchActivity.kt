package indi.dmzz_yyhyy.lightnovelreader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import indi.dmzz_yyhyy.lightnovelreader.data.ReadingBookRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeActivity
import indi.dmzz_yyhyy.lightnovelreader.ui.theme.LightNovelReaderTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LaunchActivity : ComponentActivity() {
    @Inject
    lateinit var readingBookRepository: ReadingBookRepository
    private val scope = CoroutineScope(Dispatchers.Main)
    private var context: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scope.launch {
            readingBookRepository.loadReadingBookList()
            readingBookRepository.readingBookList.collect{
                if (context!=null) {
                    val intent = Intent(context, HomeActivity::class.java)
                    ContextCompat.startActivity(context!!, intent, Bundle())
                }
            }
        }
        setContent {
            LightNovelReaderTheme {
                context = LocalContext.current
                Box(Modifier
                    .fillMaxSize()
                    .background(Color(0xFF6B94FF)),
                        contentAlignment = Alignment.Center){
                    Image(
                        modifier = Modifier.size(108.dp, 108.dp),
                        painter = painterResource(id = R.drawable.icon_foreground),
                        contentDescription = stringResource(id = R.string.desc_icon_image),
                        contentScale = ContentScale.Crop)
                }
            }
        }}

    override fun onDestroy() {
        super.onDestroy()
        // 在Activity销毁时取消协程作用域，避免内存泄漏
        scope.cancel()
    }
}