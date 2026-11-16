package io.nightfish.lightnovelreader.api.content.component.data.cap

import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData

interface ComponentDataCap<Self>
        where Self : AbstractContentComponentData,
              Self : ComponentDataCap<Self>