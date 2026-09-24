package io.nightfish.lightnovelreader.api.content.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData

interface ComponentRender {
    @Composable
    fun Component(
        modifier: Modifier = Modifier,
        componentData: AbstractContentComponentData
    )
}