package io.nightfish.lightnovelreader.api.userdata

/**
 * 用户数据路径定义
 * 以密封类层级结构组织所有预定义的用户数据路径
 * 建议通过此类中的预定义路径访问用户数据, 而非手写路径字符串
 *
 * @since Api 2
 */
@Suppress("unused")
sealed class UserDataPath(
    private val name: String,
    private val parent: UserDataPath? = null,
) {
    /**
     * 该节点的完整路径字符串，以点号分隔父节点
     *
     * @since Api 2
     */
    open val path: String get() = "${parent?.path?.plus(".") ?: ""}$name"

    /**
     * 当前节点以下所有子节点的路径字符串列表
     *
     * @since Api 2
     */
    open val groupChildrenPath: MutableList<String> = emptyList<String>().toMutableList()

    /**
     * 当前节点以下所有子节点的[UserDataPath]列表
     *
     * @since Api 2
     */
    open val groupChildren: MutableList<UserDataPath> = emptyList<UserDataPath>().toMutableList()

    init {
        parent?.let {
            groupChildrenPath.add("${parent.path.plus(".")}$name")
            groupChildren.add(this)
        }
    }

    /** 阅读界面相关用户数据路径组 @since Api 2 */
    data object Reader : UserDataPath("reader") {
        /** 阅读字体大小 @since Api 2 */
        data object FontSize : UserDataPath("font_size", Reader)

        /** 阅读字体粗细 @since Api 2 */
        data object FontWeigh : UserDataPath("font_weigh", Reader)

        /** 行间距 @since Api 2 */
        data object LineHeight : UserDataPath("line_height_new", Reader)

        /** 行间距 @since Api 2 */
        data object LetterSpacing : UserDataPath("letter_spacing", Reader)

        /** 浅色模式文字颜色 @since Api 2 */
        data object TextColor : UserDataPath("text_color", Reader)

        /** 深色模式文字颜色 @since Api 2 */
        data object TextDarkColor : UserDataPath("text_dark_color", Reader)

        /** 段前间距 @since Api 2 */
        data object SpacingBeforeParagraph : UserDataPath("spacing_before_paragraph", Reader)

        /** 段后间距 @since Api 2 */
        data object SpacingAfterParagraph : UserDataPath("spacing_after_paragraph", Reader)

        /** 首行缩进 @since Api 2 */
        data object FirstLineTextIndent : UserDataPath("first_line_text_indent", Reader)

        /** 字体文件URI路径 @since Api 2 */
        data object FontUri : UserDataPath("font_uri", Reader)

        /** 保持屏幕常亮 @since Api 2 */
        data object KeepScreenOn : UserDataPath("keep_screen_on", Reader)

        /** 启用隐藏状态栏 @since Api 2 */
        data object EnableHideStatusBar : UserDataPath("enable_hide_status_bar", Reader)

        /** 启用背景图片 @since Api 2 */
        data object EnableBackgroundImage : UserDataPath("enable_background_image", Reader)

        /** 背景图片显示模式 @since Api 2 */
        data object BackgroundImageDisplayMode :
            UserDataPath("background_image_display_mode", Reader)

        /** 是否使用翻页模式 @since Api 2 */
        data object IsUsingFlipPage : UserDataPath("is_using_flip_page", Reader)

        /** 是否使用点击翻页 @since Api 2 */
        data object IsUsingClickFlipPage : UserDataPath("is_using_click_flip_page", Reader)


        /** 是否使用音量键翻页 @since Api 2 */
        data object IsUsingVolumeKeyFlip : UserDataPath("is_using_volume_key_flip", Reader)

        /** 音量键连续翻页间隔 @since Api 2 */
        data object VolumeKeyContinuousFlipInterval :
            UserDataPath("volume_key_continuous_flip_interval", Reader)

        /** 翻页动画效果 @since Api 2 */
        data object FlipAnime : UserDataPath("flip_anime", Reader)

        /** 电量指示器显示模式 @since Api 2 */
        data object BatteryIndicatorDisplayMode :
            UserDataPath("battery_indicator_display_mode", Reader)

        /** 显示时间指示器 @since Api 2 */
        data object EnableTimeIndicator : UserDataPath("enable_time_indicator", Reader)

        /** 显示章节标题指示器 @since Api 2 */
        data object EnableChapterTitleIndicator :
            UserDataPath("enable_chapter_title_indicator", Reader)

        /** 显示章节阅读进度指示器 @since Api 2 */
        data object EnableReadingChapterProgressIndicator :
            UserDataPath("enable_reading_chapter_progress_indicator", Reader)

        /** 启用简繁体转换 @since Api 2 */
        data object EnableSimplifiedTraditionalTransform :
            UserDataPath("enable_simplified_traditional_transform", Reader)

        /** 自动内边距 @since Api 2 */
        data object AutoPadding : UserDataPath("auto_padding", Reader)

        /** 顶部内边距 @since Api 2 */
        data object TopPadding : UserDataPath("top_padding", Reader)

        /** 底部内边距 @since Api 2 */
        data object BottomPadding : UserDataPath("bottom_padding", Reader)

        /** 左侧内边距 @since Api 2 */
        data object LeftPadding : UserDataPath("left_padding", Reader)

        /** 右侧内边距 @since Api 2 */
        data object RightPadding : UserDataPath("right_padding", Reader)

        /** 浅色模式背景颜色 @since Api 2 */
        data object BackgroundColor : UserDataPath("background_color", Reader)

        /** 深色模式背景颜色 @since Api 2 */
        data object BackgroundDarkColor : UserDataPath("background_dark_color", Reader)

        /** 浅色模式背景图片URI @since Api 2 */
        data object BackgroundImageUri : UserDataPath("background_image_uri", Reader)

        /** 深色模式背景图片URI @since Api 2 */
        data object BackgroundDarkImageUri : UserDataPath("background_dark_image_uri", Reader)

        /** 返回键阻断模式 @since Api 2 */
        data object BackBlockMode : UserDataPath("back_block_mode", Reader)
    }

    /** 当前正在阅读的书籍列表路径 @since Api 2 */
    data object ReadingBooks : UserDataPath("reading_books")

    /** 书架排序路径 @since Api 4 */
    data object BookshelfOrder : UserDataPath("bookshelf_order")

    /** 搜索相关用户数据路径组 @since Api 2 */
    data object Search : UserDataPath("search") {
        /** 搜索历史记录 @since Api 2 */
        data object History : UserDataPath("history", Search)
    }

    /** 设置相关用户数据路径组 @since Api 2 */
    data object Settings : UserDataPath("settings") {
        /** 应用设置路径组 @since Api 2 */
        data object App : UserDataPath("app", Settings) {
            /** 是否自动检查更新 @since Api 2 */
            data object AutoCheckUpdate : UserDataPath("auto_check_update", App)

            /** 更新渠道 @since Api 2 */
            data object UpdateChannel : UserDataPath("update_channel", App)

            /** 发行平台 @since Api 2 */
            data object DistributionPlatform : UserDataPath("update_platform", App)

            /** 代理服务器URL @since Api 2 */
            data object ProxyUrl : UserDataPath("proxy_url", App)

            /** 统计数据 @since Api 2 */
            data object Statistics : UserDataPath("statistics", App)

            /** 最大缓存数量 @since Api 2 */
            data object MaxCache : UserDataPath("max_cache", App)
        }

        /** 显示设置路径组 @since Api 2 */
        data object Display : UserDataPath("display", Settings) {
            /** 深色模式设置 @since Api 2 */
            data object DarkMode : UserDataPath("dark_mode", Display)

            /** 动态颜色设置 @since Api 2 */
            data object DynamicColors : UserDataPath("dynamic_color", Display)

            /** 应用语言设置 @since Api 2 */
            data object AppLocale : UserDataPath("app_locale", Display)

            /** 浅色主题名称 @since Api 2 */
            data object LightThemeName : UserDataPath("light_theme_name", Display)

            /** 深色主题名称 @since Api 2 */
            data object DarkThemeName : UserDataPath("dark_theme_name", Display)

            /** 日期显示样式 @since Api 2 */
            data object DateStyle : UserDataPath("date_style", Display)

            /** 日期是否显示年份 @since Api 2 */
            data object DateShowYear : UserDataPath("date_show_year", Display)

            /** 日期排序方式 @since Api 2 */
            data object DateOrder : UserDataPath("date_order", Display)

            /** 相对时间显示样式 @since Api 2 */
            data object RelativeTimeStyle : UserDataPath("relative_time_style", Display)
        }

        /** 数据设置路径组 @since Api 2 */
        data object Data : UserDataPath("data", Settings) {
            /** 网页数据源ID @since Api 2 */
            data object WebDataSourceId : UserDataPath("web_data_source_id", Data)

            /** 日志级别设置 @since Api 2 */
            data object LogLevel : UserDataPath("log_level", Data)

            /** 是否使用代理 @since Api 2 */
            data object IsUseProxy : UserDataPath("is_use_proxy", Data)

            /** 存储统计快照缓存 @since Api 4 */
            data object StorageUsageSnapshot : UserDataPath("storage_usage_snapshot", Data)
        }
    }

    /** 已完成下载的书籍列表路径 @since Api 2 */
    data object CompletedDownloadBookList : UserDataPath("completed_download_book_list")

    /** 插件相关用户数据路径组 @since Api 2 */
    data object Plugin : UserDataPath("plugin") {
        /** 已启用的插件列表 @since Api 2 */
        data object EnabledPlugins : UserDataPath("enabled_plugins", Plugin)
    }

    /** 本地书籍相关用户数据路径组 @since Api 2 */
    data object LocalBook : UserDataPath("local_book") {
        /** 本地书籍 ID 列表 @since Api 2 */
        data object LocalBookIds : UserDataPath("local_book_ids")
    }
}
