package io.nightfish.lightnovelreader.api.content.component.render.cap

import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender

/**
 * 组件渲染能力的基础接口
 * 用于描述渲染器公开的测绘等能力
 *
 * @param Self 渲染器自身类型
 * @param Data 渲染器处理的组件数据类型
 *
 * @since Api 4
 */
interface RenderCap<Self, Data>
        where Self : AbstractContentComponentRender<Self, Data>,
              Self : RenderCap<Self, Data>,
              Data : AbstractContentComponentData