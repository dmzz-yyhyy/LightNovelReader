package io.nightfish.lightnovelreader.api.content.component.data

/**
 * 文本控件数据原件
 * 实现该接口会自动接入软件的文本后处理器
 *
 * @since Api 4
 */
interface TextData<Self>
        where Self : AbstractContentComponentData,
              Self : TextData<Self>
{
    /**
     * 数据类需要实现的文本处理函数
     *
     * @param processor 传入的文本处理器
     * @return 处理后的内容数据
     *
     * @since Api 4
     */
    fun processText(processor: (text: String) -> String): Self
}