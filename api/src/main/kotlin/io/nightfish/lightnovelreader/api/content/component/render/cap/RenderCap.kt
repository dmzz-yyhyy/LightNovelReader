package io.nightfish.lightnovelreader.api.content.component.render.cap

import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender

interface RenderCap<Self, Data>
        where Self : AbstractContentComponentRender<Self, Data>,
              Self : RenderCap<Self, Data>,
              Data : AbstractContentComponentData