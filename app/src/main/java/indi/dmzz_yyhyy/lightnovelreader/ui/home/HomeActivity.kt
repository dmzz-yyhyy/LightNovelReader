package indi.dmzz_yyhyy.lightnovelreader.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.UpdateRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.RouteConfig
import indi.dmzz_yyhyy.lightnovelreader.ui.components.UpdateAlertDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.splash.SplashViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.theme.LightNovelReaderTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    @Inject
    lateinit var updateRepository: UpdateRepository


    private val scope = CoroutineScope(Dispatchers.Main)
    private val splashViewModel: SplashViewModel by viewModels()
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition{splashViewModel.isLoading.value}
        super.onCreate(savedInstanceState)
        setContent {
            val isNeedUpdate = remember { mutableStateOf(false) }
            val isDownloadingUpdate = remember { mutableStateOf(false) }
            scope.launch {
                updateRepository.updateDetection(this@HomeActivity)
                isNeedUpdate.value = updateRepository.isNeedUpdate.value
            }
            LightNovelReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    var selectedItem by remember { mutableIntStateOf(0) }
                    if (isNeedUpdate.value) {
                        UpdateAlertDialog(
                            onDismissRequest = {
                                isNeedUpdate.value = false
                            },
                            onConfirmation = {
                                isNeedUpdate.value = false
                                isDownloadingUpdate.value = true
                                scope.launch { updateRepository.updateApp(this@HomeActivity) }
                                scope.launch {
                                    while (!updateRepository.isOver.value){
                                        updateRepository.isOver.collect {
                                            if (updateRepository.isOver.value) {
                                                isDownloadingUpdate.value = false
                                            }
                                        }
                                    }}
                            },
                            dialogTitle = stringResource(id = R.string.new_update_available),
                            dialogText = stringResource(id = R.string.new_update_available_text),
                        )
                    }
                    if (isDownloadingUpdate.value) {
                        AlertDialog(
                            icon = { Icon(Icons.Default.Download, contentDescription = "download") },
                            title = { Text(stringResource(id = R.string.downloading), style = MaterialTheme.typography.headlineSmall) },
                            text = { Text(stringResource(id = R.string.downloading_text)) },
                            onDismissRequest = {},
                            confirmButton = {},
                            dismissButton = {}
                        )}
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.reading), null) },
                                    label = { Text(stringResource(id = R.string.nav_reading)) },
                                    selected = selectedItem == 0,
                                    onClick = {
                                        selectedItem = 0
                                        navController.navigate(RouteConfig.READING)
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.bookcase), null) },
                                    label = { Text(stringResource(id = R.string.nav_bookcase)) },
                                    selected = selectedItem == 1,
                                    onClick = {
                                        selectedItem = 1
                                        navController.navigate(RouteConfig.BOOKCASE)
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.search), null) },
                                    label = { Text(stringResource(id = R.string.nav_discover)) },
                                    selected = selectedItem == 2,
                                    onClick = {
                                        selectedItem = 2
                                        navController.navigate(RouteConfig.SEARCH)
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.mine), null) },
                                    label = { Text(stringResource(id = R.string.nav_profile)) },
                                    selected = selectedItem == 3,
                                    onClick = {
                                        selectedItem = 3
                                        navController.navigate(RouteConfig.MINE)
                                    }
                                )
                            } }
                    ) {
                        Box(Modifier.padding(it)) {
                            val searchViewModel = hiltViewModel<SearchViewModel>()
                        NavHost(
                            navController = navController,
                            startDestination = RouteConfig.READING,
                        ) {
                            composable(route = RouteConfig.READING) {
                                val readingViewModel = hiltViewModel<ReadingViewModel>()
                                ReadingScreen(navController, readingViewModel)
                            }
                            composable(route = RouteConfig.BOOKCASE) {
                                BookcaseScreen(navController)
                            }
                            composable(route = RouteConfig.SEARCH) {
                                SearchScreen(navController, searchViewModel)
                            }
                            composable(route = RouteConfig.MINE) {
                                MineScreen(navController)
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}

