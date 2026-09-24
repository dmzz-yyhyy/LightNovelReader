package indi.dmzz_yyhyy.lightnovelreader

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.update.UpdateCheckRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.data.work.CheckUpdateWork
import indi.dmzz_yyhyy.lightnovelreader.theme.LightNovelReaderTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LightNovelReaderApp
import indi.dmzz_yyhyy.lightnovelreader.utils.FormattingSettings
import indi.dmzz_yyhyy.lightnovelreader.utils.LogUtils
import indi.dmzz_yyhyy.lightnovelreader.utils.combine
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import io.nightfish.lightnovelreader.api.ui.LocalComponentRender
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var loggerRepository: LoggerRepository
    @Inject
    lateinit var bookshelfRepository: BookshelfRepository

    private val intentChannel =
        Channel<Intent>(capacity = 4, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val intentFlow = intentChannel.receiveAsFlow()

    @Inject
    lateinit var userDataRepository: UserDataRepository
    @Inject
    lateinit var updateCheckRepository: UpdateCheckRepository
    @Inject
    lateinit var workManager: WorkManager
    @Inject
    lateinit var pluginManager: PluginManager
    @Inject
    lateinit var webBookDataSourceProvider: WebBookDataSourceProvider
    @Inject
    lateinit var contentComponentRepository: ContentComponentRepository
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private var appLocale by mutableStateOf(
        Resources.getSystem().configuration.locales[0].let { "${it.language}-${it.country}" }
    )
    private var darkMode by mutableStateOf("FollowSystem")
    private var dynamicColor by mutableStateOf(false)
    private var lightThemeName by mutableStateOf("light_default")
    private var darkThemeName by mutableStateOf("dark_default")

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(LogUtils(applicationContext, loggerRepository))

        workManager.enqueueUniquePeriodicWork(
            "checkUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<CheckUpdateWork>(12, TimeUnit.HOURS)
                .build()
        )
        initDefaultBookshelf()
        observeDisplaySettings()
        observeFormattingSettings()
        handleIntent(intent)

        contentComponentRepository.initRegister()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { /* Android 13 + */
            if (ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(POST_NOTIFICATIONS), 0
                )
            }
        }

        val webBookDataSourceFoundedFlow = flow {
            emit(webBookDataSourceProvider.isWebDataSourceFounded())
        }
        val fontSizeUserData = userDataRepository.floatUserData(UserDataPath.Reader.FontSize.path)
        val fontWeightUserData = userDataRepository.floatUserData(UserDataPath.Reader.FontWeigh.path)
        val lineHeightUserData =
            userDataRepository.floatUserData(UserDataPath.Reader.LineHeight.path)
        val letterSpacingUserData =
            userDataRepository.floatUserData(UserDataPath.Reader.LetterSpacing.path)
        val textColorUserData = userDataRepository.colorUserData(UserDataPath.Reader.TextColor.path)
        val textDarkColorUserData =
            userDataRepository.colorUserData(UserDataPath.Reader.TextDarkColor.path)
        val spacingBeforeParagraphUserData =
            userDataRepository.floatUserData(UserDataPath.Reader.SpacingBeforeParagraph.path)
        val spacingAfterParagraphUserData =
            userDataRepository.floatUserData(UserDataPath.Reader.SpacingAfterParagraph.path)
        val firstLineTextIndentUserData =
            userDataRepository.floatUserData(UserDataPath.Reader.FirstLineTextIndent.path)
        val fontUriUserData = userDataRepository.uriUserData(UserDataPath.Reader.FontUri.path)
        setContent {
            val readerStyle by remember {
                val default = ReaderStyle()
                combine(
                    fontSizeUserData.getFlowWithDefault(default.fontSize.value),
                    fontWeightUserData.getFlowWithDefault(default.fontWeight.weight.toFloat()),
                    lineHeightUserData.getFlowWithDefault(default.lineHeight.value),
                    letterSpacingUserData.getFlowWithDefault(default.letterSpacing.value),
                    textColorUserData.getFlowWithDefault(default.textColor),
                    textDarkColorUserData.getFlowWithDefault(default.textDarkColor),
                    spacingBeforeParagraphUserData.getFlowWithDefault(default.spacingBeforeParagraph.value),
                    spacingAfterParagraphUserData.getFlowWithDefault(default.spacingAfterParagraph.value),
                    firstLineTextIndentUserData.getFlowWithDefault(default.textIndent.firstLine.value),
                    fontUriUserData.getFlow(),
                ) { fontSize,
                    fontWeight,
                    lineHeight,
                    letterSpacing,
                    textColor,
                    textDarkColor,
                    spacingBeforeParagraph,
                    spacingAfterParagraph,
                    firstLineTextIndent,
                    fontUri ->
                    ReaderStyle(
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight(fontWeight.toInt()),
                        lineHeight = lineHeight.em,
                        letterSpacing = letterSpacing.sp,
                        textColor = textColor,
                        textDarkColor = textDarkColor,
                        spacingBeforeParagraph = spacingBeforeParagraph.sp,
                        spacingAfterParagraph = spacingAfterParagraph.sp,
                        textIndent = TextIndent(
                            firstLine = firstLineTextIndent.em
                        ),
                        fontUri = fontUri,
                    )
                }
            }.collectAsStateWithLifecycle(initialValue = ReaderStyle())
            CompositionLocalProvider(
                LocalComponentRender provides contentComponentRepository
            ) {
                LightNovelReaderTheme(
                    darkMode = darkMode,
                    appLocale = appLocale,
                    isDynamicColor = dynamicColor,
                    lightThemeName = lightThemeName,
                    darkThemeName = darkThemeName
                ) {
                    LightNovelReaderApp(
                        readerStyle = readerStyle,
                        intentFlow = intentFlow,
                        onBuildNavHost = {
                            with(pluginManager) {
                                onBuildNavHost()
                            }
                        },
                        imageHeaderGetter = { webBookDataSourceProvider.value.imageHeader },
                        webBookDataSourceFoundedFlow = webBookDataSourceFoundedFlow
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentChannel.trySend(intent)
    }

    private fun handleIntent(intent: Intent) {
        intentChannel.trySend(intent)
    }

    private fun initDefaultBookshelf() {
        coroutineScope.launch(Dispatchers.IO) {
            if (bookshelfRepository.getAllBookshelfIds().isEmpty())
                bookshelfRepository.addBookshelf(
                    Bookshelf(
                        id = 1145140721,
                        name = "已收藏",
                        sortType = BookshelfSortType.Default,
                        sortReversed = false,
                        autoCache = false,
                        systemUpdateReminder = false
                    )
                )
        }
    }

    private fun observeDisplaySettings() {
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.AppLocale.path)
                .getFlow()
                .collect { value ->
                    val locale = Resources.getSystem().configuration.locales[0]
                    val systemLocale = "${locale.language}-${locale.country}"
                    appLocale = if (value.isNullOrBlank() || value == "none") systemLocale
                    else value
                }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkMode.path).getFlow()
                .collect {
                    it?.let { darkMode = it }
                }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.LightThemeName.path)
                .getFlow().collect {
                it?.let { lightThemeName = it }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkThemeName.path)
                .getFlow().collect {
                it?.let { darkThemeName = it }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            coroutineScope.launch(Dispatchers.IO) {
                userDataRepository.booleanUserData(UserDataPath.Settings.Display.DynamicColors.path)
                    .getFlow().collect {
                    dynamicColor = it == true
                }
            }
        }
    }

    private fun observeFormattingSettings() {
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.DateStyle.path)
                .getFlow().collect {
                it?.let { FormattingSettings.dateFormat = it }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.booleanUserData(UserDataPath.Settings.Display.DateShowYear.path)
                .getFlow().collect {
                it?.let { FormattingSettings.dateShowYear = it }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.DateOrder.path)
                .getFlow().collect {
                it?.let { FormattingSettings.dateOrder = it }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.booleanUserData(UserDataPath.Settings.Display.RelativeTimeStyle.path)
                .getFlow().collect {
                it?.let { FormattingSettings.useRelativeTime = it }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
