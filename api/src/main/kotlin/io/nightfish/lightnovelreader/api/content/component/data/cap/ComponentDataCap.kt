package io.nightfish.lightnovelreader.api.content.component.data.cap

import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData

/**
 * 组件数据能力的基础接口
 *
 * @param Self 组件数据自身类型
 *
 * @since Api 4
 */
interface ComponentDataCap<Self>
        where Self : AbstractContentComponentData,
              Self : ComponentDataCap<Self>