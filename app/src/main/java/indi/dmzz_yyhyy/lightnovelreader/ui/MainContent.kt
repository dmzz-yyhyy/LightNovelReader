package indi.dmzz_yyhyy.lightnovelreader.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.R

object RouteConfig {
    const val READING = "reading"
    const val BOOKCASE = "bookcase"
    const val SEARCH = "search"
    const val MINE = "mine"
}

object Labels {
    const val READING = "阅读中"
    const val BOOKCASE = "书架"
    const val SEARCH = "探索"
    const val MINE = "我的"
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainContent() {
    var selectedItem by remember { mutableStateOf(0) }
    var selectedItemName = "阅读中"
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopAppBar(title = {Text(selectedItemName)}) },
        bottomBar = {NavigationBar {
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.reading), null) },
            label = { Text("阅读中")},
            selected = selectedItem == 0,
            onClick = {
                selectedItem = 0
                selectedItemName = Labels.READING
                navController.navigate(RouteConfig.READING)
            }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.bookcase), null) },
            label = { Text("书架") },
            selected = selectedItem == 1,
            onClick = {
                selectedItem = 1
                selectedItemName = Labels.BOOKCASE
                navController.navigate(RouteConfig.BOOKCASE)
            }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.reading), null) },
            label = { Text("探索") },
            selected = selectedItem == 2,
            onClick = {
                selectedItem = 2
                selectedItemName = Labels.SEARCH
                navController.navigate(RouteConfig.SEARCH)
            }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.mine), null) },
            label = { Text("我的") },
            selected = selectedItem == 3,
            onClick = {
                selectedItem = 3
                selectedItemName = Labels.MINE
                navController.navigate(RouteConfig.MINE)
            }
        )
    }}) {contentPadding ->
        NavHost(
            navController = navController,
            startDestination = RouteConfig.READING,
        ) {
            composable(route = RouteConfig.READING) {
                ReadingFragment()
            }
            composable(route = RouteConfig.BOOKCASE) {
                BookcaseFragment()
            }
        }
        Box(modifier = Modifier.padding(contentPadding))
    }
}