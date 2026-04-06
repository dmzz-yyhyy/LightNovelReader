package io.nightfish.lightnovelreader.api.userdata

@Suppress("unused")
sealed class UserDataPath(
    private val name: String,
    private val parent: UserDataPath? = null,
) {
    open val path: String get() = "${parent?.path?.plus(".") ?: ""}$name"
    open val groupChildrenPath: MutableList<String> = emptyList<String>().toMutableList()
    open val groupChildren: MutableList<UserDataPath> = emptyList<UserDataPath>().toMutableList()
    init {
        parent?.let {
            groupChildrenPath.add("${parent.path.plus(".")}$name")
            groupChildren.add(this)
        }
    }
    data object Reader : UserDataPath("reader") {
        data object FontSize : UserDataPath("fontSize",Reader)
        data object FontLineHeight : UserDataPath("fontLineHeight", Reader)
        data object FontWeigh : UserDataPath("fontWeigh", Reader)
        data object KeepScreenOn : UserDataPath("keepScreenOn", Reader)
        data object EnableHideStatusBar : UserDataPath("enableHideStatusBar", Reader)
        data object EnableBackgroundImage : UserDataPath("enableBackgroundImage", Reader)
        data object BackgroundImageDisplayMode : UserDataPath("backgroundImageDisplayMode", Reader)
        data object IsUsingFlipPage : UserDataPath("isUsingFlipPage", Reader)
        data object IsUsingClickFlipPage : UserDataPath("isUsingClickFlipPage", Reader)
        data object IsUsingContinuousScrolling : UserDataPath("isUsingContinuousScrolling", Reader)
        data object IsUsingVolumeKeyFlip : UserDataPath("isUsingVolumeKeyFlip", Reader)
        data object VolumeKeyContinuousFlipInterval : UserDataPath("volumeKeyContinuousFlipInterval", Reader)
        data object FlipAnime : UserDataPath("flipAnime", Reader)
        data object FastChapterChange : UserDataPath("fastChapterChange", Reader)
        data object BatteryIndicatorDisplayMode : UserDataPath("batteryIndicatorDisplayMode", Reader)
        data object EnableTimeIndicator : UserDataPath("enableTimeIndicator", Reader)
        data object EnableChapterTitleIndicator : UserDataPath("enableChapterTitleIndicator", Reader)
        data object EnableReadingChapterProgressIndicator : UserDataPath("enableReadingChapterProgressIndicator", Reader)
        data object EnableSimplifiedTraditionalTransform : UserDataPath("enableSimplifiedTraditionalTransform", Reader)
        data object AutoPadding : UserDataPath("autoPadding", Reader)
        data object TopPadding : UserDataPath("topPadding", Reader)
        data object BottomPadding : UserDataPath("bottomPadding", Reader)
        data object LeftPadding : UserDataPath("leftPadding", Reader)
        data object RightPadding : UserDataPath("rightPadding", Reader)
        data object TextColor : UserDataPath("textColor", Reader)
        data object TextDarkColor : UserDataPath("textDarkColor", Reader)
        data object FontFamilyUri : UserDataPath("fontFamilyUri", Reader)
        data object BackgroundColor : UserDataPath("backgroundColor", Reader)
        data object BackgroundDarkColor : UserDataPath("backgroundDarkColor", Reader)
        data object BackgroundImageUri : UserDataPath("backgroundImageUri", Reader)
        data object BackgroundDarkImageUri : UserDataPath("backgroundDarkImageUri", Reader)
        data object BackBlockMode : UserDataPath("backBlockMode", Reader)
    }
    data object ReadingBooks : UserDataPath("reading_books")
    data object Search: UserDataPath("search") {
        data object History : UserDataPath("history", Search)
    }
    data object Settings: UserDataPath("settings") {
        data object App : UserDataPath("app", Settings) {
            data object AutoCheckUpdate : UserDataPath("auto_check_update", App)
            data object UpdateChannel: UserDataPath("update_channel", App)
            data object DistributionPlatform: UserDataPath("update_platform", App)
            data object ProxyUrl: UserDataPath("proxy_url", App)
            data object Statistics : UserDataPath("statistics", App)
            data object MaxCache : UserDataPath("max_cache", App)
        }
        data object Display: UserDataPath("display", Settings) {
            data object DarkMode : UserDataPath("dark_mode", Display)
            data object DynamicColors : UserDataPath("dynamic_color", Display)
            data object enableCostumeThemeScheme : UserDataPath("enable_costume_theme_scheme", Display)
            data object CostumeThemeSurface : UserDataPath("costume_theme_surface", Display)
            data object CostumeThemeBackground : UserDataPath("costume_theme_background", Display)
            data object CostumeThemePrimary : UserDataPath("costume_theme_primary", Display)
            data object EnableM3E : UserDataPath("enable_m3_expressive", Display)
            data object AppLocale : UserDataPath("app_locale", Display)
            data object LightThemeName : UserDataPath("light_theme_name", Display)
            data object DarkThemeName : UserDataPath("dark_theme_name", Display)
            data object DateStyle : UserDataPath("date_style", Display)
            data object DateShowYear : UserDataPath("date_show_year", Display)
            data object DateOrder : UserDataPath("date_order", Display)
            data object RelativeTimeStyle : UserDataPath("relative_time_style", Display)
        }
        data object Data: UserDataPath("data", Settings) {
            data object WebDataSourceId : UserDataPath("web_data_source_id", Data)
            data object LogLevel : UserDataPath("log_level", Data)
            data object IsUseProxy : UserDataPath("is_use_proxy", Data)
        }
    }
    data object CompletedDownloadBookList: UserDataPath("completedDownloadBookList")
    data object Plugin: UserDataPath("plugin") {
        data object EnabledPlugins: UserDataPath("enabledPlugins", Plugin)
    }
    data object LocalBook: UserDataPath("localBook") {
        data object LocalBookIds: UserDataPath("localBookIds")
    }
}
