package io.nightfish.lightnovelreader.api

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/**
 * 应用内所有导航路由的定义对象
 * 使用 Kotlin 序列化实现导航路由
 * 插件可通过[LightNovelReaderPlugin][io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin]的导航功能进行页面跳转
 *
 * @since Api 2
 */
@Keep
object Route {
    /** 主界面导航路由组 */
    @Serializable
    object Main {
        /** 阅读相关界面路由组 */
        @Serializable
        object Reading {
            /** 阅读主界面路由 */
            @Serializable
            object Home
            /** 阅读统计界面路由组 */
            @Serializable
            object Stats {
                /** 阅读统计总览界面路由 */
                @Serializable
                object Overview
                /**
                 * 阅读统计详情界面路由
                 *
                 * @param targetDate 目标日期，以整数格式表示（yyyyMMdd）
                 */
                @Serializable
                data class Detailed(val targetDate: Int)
            }
        }
        /** 书架界面路由组 */
        @Serializable
        object Bookshelf {
            /** 书架主界面路由 */
            @Serializable
            object Home
            /** 书本排序界面路由
             *
             * @param id 书架id
             */
            @Serializable
            data class ReorderBooks(
                val id: Int
            )
            /** 书架排序界面路由 */
            @Serializable
            object ReorderBookshelves
            /**
             * 书架编辑界面路由
             *
             * @param id 书架id
             * @param title 书架名称
             */
            @Serializable
            data class Edit(
                val id: Int,
                val title: String
            )
            /**
             * 删除书架确认对话框路由
             *
             * @param bookshelfId 目标书架id
             */
            @Serializable
            data class DeleteBookshelfDialog(
                val bookshelfId: Int
            )
            /**
             * 将多本书添加至书架的对话框路由
             *
             * @param selectedBookIds 待添加的书本id列表
             */
            @Serializable
            data class AddBookToBookshelfDialog(
                val selectedBookIds: List<String>
            )
        }
        /** 探索界面路由组 */
        @Serializable
        object Explore {
            /** 探索主界面路由 */
            @Serializable
            object Home
            /** 搜索界面路由 */
            @Serializable
            object Search
            /**
             * 探索展开页界面路由
             *
             * @param expandedPageDataSourceId 展开页数据源的唯一标识
             */
            @Serializable
            data class Expanded(
                val expandedPageDataSourceId: String
            )
        }
        /** 设置界面路由组 */
        @Serializable
        object Settings {
            /** 设置主界面路由 */
            @Serializable
            object Home
            /** 日志查看界面路由 */
            @Serializable
            object Logcat
            /** 文本格式化设置界面路由组 */
            @Serializable
            object TextFormatting {
                /** 文本格式化规则管理界面路由 */
                @Serializable
                object Manager
                /**
                 * 文本格式化规则列表界面路由
                 *
                 * @param bookId 目标书本id
                 */
                @Serializable
                data class Rules(val bookId: String)
            }
            /** 插件管理界面路由组 */
            @Serializable
            object PluginManager {
                /** 插件管理主界面路由 */
                @Serializable
                object Home
                /**
                 * 插件详情界面路由
                 *
                 * @param id 插件的唯一标识
                 */
                @Serializable
                data class Detail(
                    val id: String
                )
                /** 已安装插件列表界面路由 */
                @Serializable
                object AppList
            }
            /** 数据源切换界面路由组 */
            @Serializable
            object SourceChange {
                /** 数据源切换列表界面路由 */
                @Serializable
                object List
                /**
                 * 数据源详细设置界面路由
                 *
                 * @param sourceId 目标数据源的唯一标识
                 */
                @Serializable
                data class Settings(
                    val sourceId: String
                )
            }
            /** 调试信息界面路由 */
            @Serializable
            object Debug
            /** 主题设置界面路由 */
            @Serializable
            object Theme
            /** 开源许可证界面路由 */
            @Serializable
            object Licenses
            /** 支持格式信息界面路由 */
            @Serializable
            object Formats
        }
        /** 导出用户数据对话框路由 */
        @Serializable
        object ExportUserDataDialog
        /**
         * 编辑文本格式化规则对话框路由
         *
         * @param bookId 目标书本id
         * @param ruleId 目标规则id
         */
        @Serializable
        data class EditTextFormattingRuleDialog(
            val bookId: String,
            val ruleId: Int
        )
    }
    /** 书本相关页面路由组 */
    @Serializable
    object Book {
        /**
         * 书本详情界面路由
         *
         * @param bookId 目标书本id
         */
        @Serializable
        data class Detail(
            val bookId: String
        )
        /** 书本阅读界面路由 */
        @Serializable
        data object Reader

        /**
         * 颜色选择器对话框路由
         *
         * @param colorUserDataPath 颜色用户数据的路径字符串
         * @param colors 可选颜色的ARGB值列表
         */
        @Serializable
        data class ColorPickerDialog(
            val colorUserDataPath: String,
            val colors: LongArray
        ) {
            /**
             * 判断两个[ColorPickerDialog]是否相等
             *
             * @param other 另一个对象
             * @return 属性完全相同则返回true
             */
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ColorPickerDialog

                if (colorUserDataPath != other.colorUserDataPath) return false
                if (!colors.contentEquals(other.colors)) return false

                return true
            }

            /**
             * 基于[colorUserDataPath]和[colors]计算哈希值
             *
             * @return 哈希值
             */
            override fun hashCode(): Int {
                var result = colorUserDataPath.hashCode()
                result = 31 * result + colors.contentHashCode()
                return result
            }
        }
        /**
         * 图片查看器对话框路由
         *
         * @param imageUri 图片的URI字符串
         */
        @Serializable
        data class ImageViewerDialog(
            val imageUri: String
        )
    }
    /** 有可用更新提示对话框路由 */
    @Serializable
    object UpdatesAvailableDialog
    /**
     * 将书本添加至书架对话框路由
     *
     * @param bookId 目标书本id
     */
    @Serializable
    data class AddBookToBookshelfDialog(
        val bookId: String
    )
    /**
     * 将所有章节标记为已读对话框路由
     *
     * @param bookId 目标书本id
     */
    @Serializable
    data class MarkAllChaptersAsReadDialog(
        val bookId: String
    )
    /**
     * 滑块数值设置对话框路由
     *
     * @param value 当前滑块数值
     * @param floatUserDataPath 关联浮点型用户数据的路径字符串
     */
    @Serializable
    data class SliderValueDialog(
        val value: Float,
        val floatUserDataPath: String
    )
    /**
     * 插件安装器对话框路由
     *
     * @param source 插件来源路径或URI字符串
     */
    @Serializable
    data class PluginInstallerDialog(
        val source: String
    )
    /** 书本管理器路由 */
    @Serializable
    object BookManager

    /** 存储空间管理器路由 */
    @Serializable
    object StorageManager

    /** 插件商店安装底栏
     *
     * @param pluginId 目标插件id
     */
    @Serializable
    data class PluginStoreInstall(val pluginId: String)
}
