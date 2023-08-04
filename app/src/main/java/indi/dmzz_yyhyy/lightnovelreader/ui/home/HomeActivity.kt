package indi.dmzz_yyhyy.lightnovelreader.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.RouteConfig
import indi.dmzz_yyhyy.lightnovelreader.ui.theme.LightNovelReaderTheme

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            LightNovelReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    var screenName by remember { mutableStateOf(getString(R.string.nav_reading)) }
                    Scaffold(
                        topBar = { TopAppBar(title = { Text(screenName) }) },
                        bottomBar = {
                            var selectedItem by remember { mutableStateOf(0) }

                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.reading), null) },
                                    label = { Text(stringResource(id = R.string.nav_reading)) },
                                    selected = selectedItem == 0,
                                    onClick = {
                                        screenName = getString(R.string.nav_reading)
                                        selectedItem = 0
                                        navController.navigate(RouteConfig.READING)
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.bookcase), null) },
                                    label = { Text(stringResource(id = R.string.nav_library)) },
                                    selected = selectedItem == 1,
                                    onClick = {
                                        screenName = getString(R.string.nav_library)
                                        selectedItem = 1
                                        navController.navigate(RouteConfig.BOOKCASE)
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.search), null) },
                                    label = { Text(stringResource(id = R.string.nav_discover)) },
                                    selected = selectedItem == 2,
                                    onClick = {
                                        screenName = getString(R.string.nav_discover)
                                        selectedItem = 2
                                        navController.navigate(RouteConfig.SEARCH)
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.mine), null) },
                                    label = { Text(stringResource(id = R.string.nav_profile)) },
                                    selected = selectedItem == 3,
                                    onClick = {
                                        screenName = getString(R.string.nav_profile)
                                        selectedItem = 3
                                        navController.navigate(RouteConfig.MINE)
                                    }
                                )
                            } }
                    ) {
                        Box(Modifier.padding(it)) {
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
                                SearchScreen(navController)
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

