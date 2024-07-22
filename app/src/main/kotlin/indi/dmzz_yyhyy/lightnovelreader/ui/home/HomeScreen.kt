package indi.dmzz_yyhyy.lightnovelreader.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.Screen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.ReadingScreen

@Composable
fun HomeScreen(
    onOpenBook: (Int) -> Unit
) {
    val navController = rememberNavController()
    var topBar : @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
    var selectedItem by remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = topBar,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        if (selectedItem == 0)
                            Icon(painter = painterResource(id = R.drawable.filled_book_24px),
                                contentDescription = null,
                                tint = if (selectedItem == 0)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant)
                        else Icon(painter = painterResource(id = R.drawable.outline_book_24px),
                            contentDescription = null,
                            tint = if (selectedItem == 0)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant) },
                    label = { Text(
                        text = "Reading",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedItem == 0)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ) },
                    selected = selectedItem == 0,
                    onClick = {
                        navController.navigate(Screen.Home.Reading.route)
                    }
                )
                NavigationBarItem(
                    icon = {
                        if (selectedItem == 1)
                            Icon(painter = painterResource(id = R.drawable.filled_shelves_24px),
                                contentDescription = null,
                                tint = if (selectedItem == 1)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant)
                        else Icon(painter = painterResource(id = R.drawable.outline_shelves_24px),
                            contentDescription = null,
                            tint = if (selectedItem == 1)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant) },
                    label = { Text(
                        text = "Bookcase",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedItem == 1)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ) },
                    selected = selectedItem == 1,
                    onClick = {
                        navController.navigate(Screen.Home.Bookshelf.route)
                    }
                )
                NavigationBarItem(
                    icon = {
                        if (selectedItem == 2)
                            Icon(painter = painterResource(id = R.drawable.filled_explore_24px),
                                contentDescription = null,
                                tint = if (selectedItem == 2)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant)
                        else Icon(painter = painterResource(id = R.drawable.outline_explore_24px),
                            contentDescription = null,
                            tint = if (selectedItem == 2)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant) },
                    label = { Text(
                        text = "Exploration",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedItem == 2)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ) },
                    selected = selectedItem == 2,
                    onClick = {
                        navController.navigate(Screen.Home.Exploration.route)
                    }
                )
                NavigationBarItem(
                    icon = {
                        if (selectedItem == 3)
                            Icon(painter = painterResource(id = R.drawable.filled_settings_24px),
                                contentDescription = null,
                                tint = if (selectedItem == 3)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant)
                        else Icon(painter = painterResource(id = R.drawable.outline_settings_24px),
                            contentDescription = null,
                            tint = if (selectedItem == 3)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant) },
                    label = { Text(
                        text = "Settings",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedItem == 3)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ) },
                    selected = selectedItem == 3,
                    onClick = {
                        navController.navigate(Screen.Home.Settings.route)
                    }
                )
            }
        }
    ) { it ->
        Box(Modifier.padding(it)) {
            NavHost(navController = navController, startDestination = Screen.Home.Reading.route) {
                composable(route = Screen.Home.Reading.route) {
                    selectedItem = 0
                    ReadingScreen(
                        onOpenBook = onOpenBook,
                        topBar = {newTopBar -> topBar = newTopBar}
                    )
                }
                composable(route = Screen.Home.Bookshelf.route) {
                    selectedItem = 1
                    Text("书架·施工中")
                }
                composable(route = Screen.Home.Exploration.route) {
                    selectedItem = 2
                    Text("探索·施工中")
                }
                composable(route = Screen.Home.Settings.route) {
                    selectedItem = 3
                    Text("设置·施工中")
                }
            }
        }
    }
}