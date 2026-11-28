package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.nightfish.lightnovelreader.api.web.explore.CustomExplorePageContent
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageContext
import kotlinx.coroutines.coroutineScope

@Composable
internal fun CustomExploreContent(page: CustomExplorePageContent, context: ExplorePageContext) {
    LaunchedEffect(page) { coroutineScope { page.init(this) } }
    page.Content(context)
}
