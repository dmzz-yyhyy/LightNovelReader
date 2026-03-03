package io.nightfish.lightnovelreader.api.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

val LocalNavController = compositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

val LocalReaderStyle = compositionLocalOf {
    ReaderStyle(
        fontSize = 15f,
        fontLineHeight = 7f,
        fontWeight = 500f,
        textColor = Color.Unspecified,
        textDarkColor = Color.Unspecified,
    )
}