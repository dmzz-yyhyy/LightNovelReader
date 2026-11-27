package io.nightfish.lightnovelreader.api.userdata


import android.net.Uri
import androidx.compose.ui.graphics.Color
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/**
 * 用户数据路径定义
 * 在路径声明中指定数据的序列化格式和归属
 * 建议通过此类中的预定义路径访问用户数据, 而非手写路径字符串
 *
 * @param T 该路径存储的值类型
 * @property path 完整路径字符串
 * @property serializer 数据的序列化器
 * @property namespace 路径所属的命名空间
 * @property sourceScoped 是否按数据源隔离数据
 *
 * @since Api 4
 */
@Serializable(UserDataPathSerializer::class)
open class UserDataPath<T>(
    val path: String,
    val serializer: KSerializer<T>,
    val namespace: String = Identifier.HOST_NAMESPACE,
    val sourceScoped: Boolean = false,
) {
    /**
     * 创建绑定归属的设置标识符
     *
     * @param source 需要隔离的数据源, 非书源路径可不传入
     * @return 以完整路径作为 id 的设置标识符
     *
     * @since Api 4
     */
    fun identifier(source: Identifier.DataSource? = null): Identifier.SettingPath =
        Identifier.SettingPath(
            if (sourceScoped) requireNotNull(source) { "Source required for $path" }.sourceNamespace else namespace,
            path
        )
    /**
     * 判断两个路径是否具有相同的存储键、归属和序列化格式
     *
     * @param other 另一个对象
     * @return 路径定义相同则返回true
     *
     * @since Api 4
     */
    final override fun equals(other: Any?): Boolean =
        other is UserDataPath<*> && path == other.path && namespace == other.namespace &&
                sourceScoped == other.sourceScoped && serializer.descriptor == other.serializer.descriptor

    /**
     * 计算路径定义的哈希值
     *
     * @return 基于存储键、归属和序列化格式的哈希值
     *
     * @since Api 4
     */
    final override fun hashCode(): Int =
        31 * (31 * (31 * path.hashCode() + namespace.hashCode()) + sourceScoped.hashCode()) + serializer.descriptor.hashCode()

    /** 阅读界面相关用户数据路径组 @since Api 2 */
    object Reader {
        /** 阅读器书写方向 @since Api 4 */
        data object WriteMode : UserDataPath<String>("reader.write_mode", String.serializer())
        /** 阅读字体大小 @since Api 2 */
        data object FontSize : UserDataPath<Float>("reader.font_size", Float.serializer())
        /** 阅读字体粗细 @since Api 2 */
        data object FontWeigh : UserDataPath<Float>("reader.font_weigh", Float.serializer())
        /** 行间距 @since Api 2 */
        data object LineHeight : UserDataPath<Float>("reader.line_height_new", Float.serializer())
        /** 行间距 @since Api 2 */
        data object LetterSpacing : UserDataPath<Float>("reader.letter_spacing", Float.serializer())
        /** 浅色模式文字颜色 @since Api 2 */
        data object TextColor : UserDataPath<Color>("reader.text_color", UserDataColorSerializer)
        /** 深色模式文字颜色 @since Api 2 */
        data object TextDarkColor :
            UserDataPath<Color>("reader.text_dark_color", UserDataColorSerializer)
        /** 段前间距 @since Api 2 */
        data object SpacingBeforeParagraph :
            UserDataPath<Float>("reader.spacing_before_paragraph", Float.serializer())
        /** 段后间距 @since Api 2 */
        data object SpacingAfterParagraph :
            UserDataPath<Float>("reader.spacing_after_paragraph", Float.serializer())
        /** 首行缩进 @since Api 2 */
        data object FirstLineTextIndent :
            UserDataPath<Float>("reader.first_line_text_indent", Float.serializer())
        /** 字体文件URI路径 @since Api 2 */
        data object FontUri : UserDataPath<Uri>("reader.font_uri", UserDataUriSerializer)
        /** 保持屏幕常亮 @since Api 2 */
        data object KeepScreenOn :
            UserDataPath<Boolean>("reader.keep_screen_on", Boolean.serializer())
        /** 启用隐藏状态栏 @since Api 2 */
        data object EnableHideStatusBar :
            UserDataPath<Boolean>("reader.enable_hide_status_bar", Boolean.serializer())
        /** 启用背景图片 @since Api 2 */
        data object EnableBackgroundImage :
            UserDataPath<Boolean>("reader.enable_background_image", Boolean.serializer())
        /** 背景图片显示模式 @since Api 2 */
        data object BackgroundImageDisplayMode :
            UserDataPath<String>("reader.background_image_display_mode", String.serializer())
        /** 是否使用翻页模式 @since Api 2 */
        data object IsUsingFlipPage :
            UserDataPath<Boolean>("reader.is_using_flip_page", Boolean.serializer())
        /** 是否使用点击翻页 @since Api 2 */
        data object IsUsingClickFlipPage :
            UserDataPath<Boolean>("reader.is_using_click_flip_page", Boolean.serializer())
        /** 是否使用音量键翻页 @since Api 2 */
        data object IsUsingVolumeKeyFlip :
            UserDataPath<Boolean>("reader.is_using_volume_key_flip", Boolean.serializer())
        /** 音量键连续翻页间隔 @since Api 2 */
        data object VolumeKeyContinuousFlipInterval :
            UserDataPath<Float>("reader.volume_key_continuous_flip_interval", Float.serializer())
        /** 翻页动画效果 @since Api 2 */
        data object FlipAnime : UserDataPath<String>("reader.flip_anime", String.serializer())
        /** 电量指示器显示模式 @since Api 2 */
        data object BatteryIndicatorDisplayMode :
            UserDataPath<String>("reader.battery_indicator_display_mode", String.serializer())
        /** 显示时间指示器 @since Api 2 */
        data object EnableTimeIndicator :
            UserDataPath<Boolean>("reader.enable_time_indicator", Boolean.serializer())
        /** 显示章节标题指示器 @since Api 2 */
        data object EnableChapterTitleIndicator :
            UserDataPath<Boolean>("reader.enable_chapter_title_indicator", Boolean.serializer())
        /** 显示章节阅读进度指示器 @since Api 2 */
        data object EnableReadingChapterProgressIndicator : UserDataPath<Boolean>(
            "reader.enable_reading_chapter_progress_indicator",
            Boolean.serializer()
        )
        /** 启用简繁体转换 @since Api 2 */
        data object EnableSimplifiedTraditionalTransform : UserDataPath<Boolean>(
            "reader.enable_simplified_traditional_transform",
            Boolean.serializer()
        )
        /** 自动内边距 @since Api 2 */
        data object AutoPadding : UserDataPath<Boolean>("reader.auto_padding", Boolean.serializer())
        /** 顶部内边距 @since Api 2 */
        data object TopPadding : UserDataPath<Float>("reader.top_padding", Float.serializer())
        /** 底部内边距 @since Api 2 */
        data object BottomPadding : UserDataPath<Float>("reader.bottom_padding", Float.serializer())
        /** 左侧内边距 @since Api 2 */
        data object LeftPadding : UserDataPath<Float>("reader.left_padding", Float.serializer())
        /** 右侧内边距 @since Api 2 */
        data object RightPadding : UserDataPath<Float>("reader.right_padding", Float.serializer())
        /** 浅色模式背景颜色 @since Api 2 */
        data object BackgroundColor :
            UserDataPath<Color>("reader.background_color", UserDataColorSerializer)
        /** 深色模式背景颜色 @since Api 2 */
        data object BackgroundDarkColor :
            UserDataPath<Color>("reader.background_dark_color", UserDataColorSerializer)
        /** 浅色模式背景图片URI @since Api 2 */
        data object BackgroundImageUri :
            UserDataPath<Uri>("reader.background_image_uri", UserDataUriSerializer)
        /** 深色模式背景图片URI @since Api 2 */
        data object BackgroundDarkImageUri :
            UserDataPath<Uri>("reader.background_dark_image_uri", UserDataUriSerializer)
        /** 返回键阻断模式 @since Api 2 */
        data object BackBlockMode :
            UserDataPath<String>("reader.back_block_mode", String.serializer())
    }
    /** 当前正在阅读的书籍列表路径 @since Api 2 */
    data object ReadingBooks : UserDataPath<List<Identifier.Book>>(
        "reading_books",
        ListSerializer(Identifier.Book.Serializer),
        sourceScoped = true
    )
    /** 书架排序路径 @since Api 4 */
    data object BookshelfOrder : UserDataPath<List<Identifier.Bookshelf>>(
        "bookshelf_order",
        ListSerializer(Identifier.Bookshelf.Serializer),
        sourceScoped = true
    )
    /** 搜索相关用户数据路径组 @since Api 2 */
    object Search {
        /** 搜索历史记录 @since Api 2 */
        data object History : UserDataPath<List<String>>(
            "search.history",
            ListSerializer(String.serializer()),
            sourceScoped = true
        )
    }
    /** 设置相关用户数据路径组 @since Api 2 */
    object Settings {
        /** 应用设置路径组 @since Api 2 */
        object App {
            /** 是否自动检查更新 @since Api 2 */
            data object AutoCheckUpdate :
                UserDataPath<Boolean>("settings.app.auto_check_update", Boolean.serializer())
            /** 更新渠道 @since Api 2 */
            data object UpdateChannel :
                UserDataPath<String>("settings.app.update_channel", String.serializer())
            /** 发行平台 @since Api 2 */
            data object DistributionPlatform :
                UserDataPath<String>("settings.app.update_platform", String.serializer())
            /** 代理服务器URL @since Api 2 */
            data object ProxyUrl :
                UserDataPath<String>("settings.app.proxy_url", String.serializer())
            /** 统计数据 @since Api 2 */
            data object Statistics :
                UserDataPath<Boolean>("settings.app.statistics", Boolean.serializer())
            /** 最大缓存数量 @since Api 2 */
            data object MaxCache : UserDataPath<Int>("settings.app.max_cache", Int.serializer())
        }
        /** 显示设置路径组 @since Api 2 */
        object Display {
            /** 深色模式设置 @since Api 2 */
            data object DarkMode :
                UserDataPath<String>("settings.display.dark_mode", String.serializer())
            /** 动态颜色设置 @since Api 2 */
            data object DynamicColors :
                UserDataPath<Boolean>("settings.display.dynamic_color", Boolean.serializer())
            /** 应用语言设置 @since Api 2 */
            data object AppLocale :
                UserDataPath<String>("settings.display.app_locale", String.serializer())
            /** 浅色主题名称 @since Api 2 */
            data object LightThemeName :
                UserDataPath<String>("settings.display.light_theme_name", String.serializer())
            /** 深色主题名称 @since Api 2 */
            data object DarkThemeName :
                UserDataPath<String>("settings.display.dark_theme_name", String.serializer())
            /** 日期显示样式 @since Api 2 */
            data object DateStyle :
                UserDataPath<String>("settings.display.date_style", String.serializer())
            /** 日期是否显示年份 @since Api 2 */
            data object DateShowYear :
                UserDataPath<Boolean>("settings.display.date_show_year", Boolean.serializer())
            /** 日期排序方式 @since Api 2 */
            data object DateOrder :
                UserDataPath<String>("settings.display.date_order", String.serializer())
            /** 相对时间显示样式 @since Api 2 */
            data object RelativeTimeStyle :
                UserDataPath<Boolean>("settings.display.relative_time_style", Boolean.serializer())
        }
        /** 数据设置路径组 @since Api 2 */
        object Data {
            /** 网页数据源ID @since Api 2 */
            data object WebDataSourceId : UserDataPath<Identifier.DataSource>(
                "settings.data.web_data_source_id",
                Identifier.DataSource.Serializer
            )
            /** 日志级别设置 @since Api 2 */
            data object LogLevel :
                UserDataPath<String>("settings.data.log_level", String.serializer())
            /** 是否使用代理 @since Api 2 */
            data object IsUseProxy :
                UserDataPath<Boolean>("settings.data.is_use_proxy", Boolean.serializer())
            /** 存储统计快照缓存 @since Api 4 */
            data object StorageUsageSnapshot :
                UserDataPath<String>("settings.data.storage_usage_snapshot", String.serializer())
        }
    }
    /** 已完成下载的书籍列表路径 @since Api 2 */
    data object CompletedDownloadBookList : UserDataPath<List<CompletedDownload>>(
        "completed_download_book_list",
        ListSerializer(CompletedDownload.serializer()),
        sourceScoped = true
    )
    /** 插件相关用户数据路径组 @since Api 2 */
    object Plugin {
        /** 已启用的插件列表 @since Api 2 */
        data object EnabledPlugins : UserDataPath<List<String>>(
            "plugin.enabled_plugins",
            ListSerializer(String.serializer())
        )
    }
    /** 本地书籍相关用户数据路径组 @since Api 2 */
    object LocalBook {
        /** 本地书籍 ID 列表 @since Api 2 */
        data object LocalBookIds : UserDataPath<List<Identifier.Book>>(
            "local_book_ids",
            ListSerializer(Identifier.Book.Serializer)
        )
    }

}

/**
 * 已完成的下载记录
 *
 * @property type 下载任务类型
 * @property bookId 书本标识符
 *
 * @since Api 4
 */
@Serializable
data class CompletedDownload(val type: String, val bookId: Identifier.Book)
