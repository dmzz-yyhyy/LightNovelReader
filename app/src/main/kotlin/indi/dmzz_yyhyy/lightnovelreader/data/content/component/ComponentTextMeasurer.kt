package indi.dmzz_yyhyy.lightnovelreader.data.content.component

import android.content.Context
import android.view.View
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

internal fun componentTextMeasurer(context: Context, density: Density) = TextMeasurer(
    defaultFontFamilyResolver = createFontFamilyResolver(context),
    defaultDensity = density,
    defaultLayoutDirection = if (
        context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    ) LayoutDirection.Rtl else LayoutDirection.Ltr,
)
