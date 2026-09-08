package io.nightfish.lightnovelreader.api

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 应用内所有导航路由的定义对象
 * 使用 Kotlin 序列化实现导航路由
 * 插件可通过[LightNovelReaderPlugin][io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin]的导航功能进行页面跳转
 *
 * @since Api 2
 */
sealed interface Route : NavKey {

    /** 主界面导航路由组 */
    @Serializable
    object Main : Route {
        /** 阅读相关界面路由组 */
        @Serializable
        object Reading {
            /** 阅读主界面路由 */
            @Serializable
            object Home : Route

            /** 阅读统计界面路由组 */
            @Serializable
            object Stats {
                /** 阅读统计总览界面路由 */
                @Serializable
                object Overview : Route

                /**
                 * 阅读统计详情界面路由
                 *
                 * @param targetDate 目标日期，以整数格式表示（yyyyMMdd）
                 */
                @Serializable
                data class Detailed(
                    val targetDate: Int
                ) : Route
            }
        }

        /** 书架界面路由组 */
        @Serializable
        object Bookshelf {
            /** 书架主界面路由 */
            @Serializable
            object Home : Route

            /**
             * 书本排序界面路由
             *
             * @param id 书架id
             */
            @Serializable
            data class ReorderBooks(
                val id: Int
            ) : Route

            /** 书架排序界面路由 */
            @Serializable
            object ReorderBookshelves : Route

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
            ) : Route

            /**
             * 删除书架确认对话框路由
             *
             * @param bookshelfId 目标书架id
             */
            @Serializable
            data class DeleteBookshelfDialog(
                val bookshelfId: Int
            ) : Route

            /**
             * 将多本书添加至书架的对话框路由
             *
             * @param selectedBookIds 待添加的书本id列表
             */
            @Serializable
            data class AddBookToBookshelfDialog(
                val selectedBookIds: List<String>
            ) : Route
        }

        /** 探索界面路由组 */
        @Serializable
        object Explore {
            /** 探索主界面路由 */
            @Serializable
            object Home : Route

            /** 搜索界面路由 */
            @Serializable
            object Search : Route

            /**
             * 探索展开页界面路由
             *
             * @param expandedPageDataSourceId 展开页数据源的唯一标识
             */
            @Serializable
            data class Expanded(
                val expandedPageDataSourceId: String
            ) : Route
        }

        /** 设置界面路由组 */
        @Serializable
        object Settings {
            /** 设置主界面路由 */
            @Serializable
            object Home : Route

            /** 日志查看界面路由 */
            @Serializable
            object Logcat : Route

            /** 文本格式化设置界面路由组 */
            @Serializable
            object TextFormatting {
                /** 文本格式化规则管理界面路由 */
                @Serializable
                object Manager : Route

                /**
                 * 文本格式化规则列表界面路由
                 *
                 * @param bookId 目标书本id
                 */
                @Serializable
                data class Rules(
                    val bookId: String
                ) : Route
            }

            /** 插件管理界面路由组 */
            @Serializable
            object PluginManager {
                /** 插件管理主界面路由 */
                @Serializable
                object Home : Route

                /**
                 * 插件详情界面路由
                 *
                 * @param id 插件的唯一标识
                 */
                @Serializable
                data class Detail(
                    val id: String
                ) : Route

                /** 已安装插件列表界面路由 */
                @Serializable
                object AppList : Route
            }

            /** 数据源切换界面路由组 */
            @Serializable
            object SourceChange {
                /** 数据源切换列表界面路由 */
                @Serializable
                object List : Route

                /**
                 * 数据源详细设置界面路由
                 *
                 * @param sourceId 目标数据源的唯一标识
                 */
                @Serializable
                data class Settings(
                    val sourceId: String
                ) : Route
            }

            /** 调试信息界面路由 */
            @Serializable
            object Debug : Route

            /** 主题设置界面路由 */
            @Serializable
            object Theme : Route

            /** 阅读样式设置界面路由 */
            @Serializable
            object ReaderStyle : Route

            /** 开源许可证界面路由 */
            @Serializable
            object Licenses : Route

            /** 支持格式信息界面路由 */
            @Serializable
            object Formats : Route
        }

        /** 导出用户数据对话框路由 */
        @Serializable
        object ExportUserDataDialog : Route

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
        ) : Route
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
        ) : Route

        /**
         * 书本阅读界面路由
         *
         * @param bookId 目标书本id
         * @param chapterId 初始章节id
         */
        @Serializable
        data class Reader(
            val bookId: String,
            val chapterId: String,
        ) : Route

        /**
         * 颜色选择器调色盘用途
         */
        interface ColorPickerTarget {
            /**
             * 描述的翻译键id
             */
            @get:StringRes
            val descriptionResId: Int
        }

        /**
         * 颜色选择器调色盘用途类型（用于路由序列化）
         */
        enum class ColorPickerTargetType {
            TEXT,
            BACKGROUND,
        }

        /**
         * 颜色选择器对话框路由
         *
         * @param colorUserDataPath 颜色用户数据的路径字符串
         * @param colors 可选颜色的ARGB值列表
         * @param target 调色盘用途类型
         */
        @Serializable
        data class ColorPickerDialog(
            val colorUserDataPath: String,
            val colors: LongArray,
            val target: ColorPickerTargetType =
                ColorPickerTargetType.BACKGROUND,
        ) : Route {

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
                if (target != other.target) return false

                return true
            }

            /**
             * 基于[colorUserDataPath]、[colors]和[target]计算哈希值
             *
             * @return 哈希值
             */
            override fun hashCode(): Int {
                var result = colorUserDataPath.hashCode()
                result = 31 * result + colors.contentHashCode()
                result = 31 * result + target.hashCode()
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
        ) : Route
    }

    /** 有可用更新提示对话框路由 */
    @Serializable
    object UpdatesAvailableDialog : Route

    /**
     * 将书本添加至书架对话框路由
     *
     * @param bookId 目标书本id
     */
    @Serializable
    data class AddBookToBookshelfDialog(
        val bookId: String
    ) : Route

    /**
     * 将所有章节标记为已读对话框路由
     *
     * @param bookId 目标书本id
     */
    @Serializable
    data class MarkAllChaptersAsReadDialog(
        val bookId: String
    ) : Route

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
    ) : Route

    /**
     * 插件安装器对话框路由
     *
     * @param source 插件来源路径或URI字符串
     */
    @Serializable
    data class PluginInstallerDialog(
        val source: String
    ) : Route

    /** 书本管理器路由 */
    @Serializable
    object BookManager : Route

    /** 存储空间管理器路由 */
    @Serializable
    object StorageManager : Route

    /** 插件商店安装底栏
     *
     * @param pluginId 目标插件id
     */
    @Serializable
    data class PluginStoreInstall(val pluginId: String) : Route
}
