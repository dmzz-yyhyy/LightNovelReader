package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data

import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.update.APIParser
import indi.dmzz_yyhyy.lightnovelreader.data.update.GithubParser
import indi.dmzz_yyhyy.lightnovelreader.data.update.UpdateParser
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType

@Suppress("PropertyName", "unused")
sealed class MenuOptions {
    protected val _optionList: MutableList<Option>
    val optionList: List<Option> get() = _optionList.toList()
    constructor(vararg options: Option) {
        _optionList = options.toMutableList()
    }
    constructor(options: List<Option>) {
        _optionList = options.toMutableList()
    }
    fun option(key: String, nameId: Int): String {
        _optionList.add(Option(key, nameId))
        return key
    }

    fun get(key: String): Option =
        getOrNull(key) ?: throw NoSuchElementException("Option '$key' not found")

    fun getOrNull(key: String): Option? =
        optionList.firstOrNull { it.equals(key) }

    fun getOrDefault(key: String, default: Option): Option =
        getOrNull(key) ?: default

    open class MenuOptionsWithValues<T> : MenuOptions {

        private val optionWithValueList: MutableList<OptionWithValue<T>>

        constructor(vararg options: OptionWithValue<T>) : super(options.toList()) {
            optionWithValueList = options.toMutableList()
        }

        constructor(options: List<OptionWithValue<T>>) : super(options) {
            optionWithValueList = options.toMutableList()
        }

        fun option(key: String, nameId: Int, value: T): String {
            _optionList.add(Option(key, nameId))
            optionWithValueList.add(OptionWithValue(key, nameId, value))
            return key
        }

        fun getOptionWithValue(key: String): OptionWithValue<T> =
            getOptionWithValueOrNull(key)
                ?: throw NoSuchElementException("OptionWithValue '$key' not found")

        fun getOptionWithValueOrNull(key: String): OptionWithValue<T>? =
            optionWithValueList.firstOrNull { it.equals(key) }

        fun getOptionWithValueOrDefault(key: String?): OptionWithValue<T> =
            getOptionWithValueOrNull(key ?: "") ?: optionWithValueList.first()
    }


    open class Option(
        open val key: String,
        open val nameId: Int
    ) {
        override fun equals(other: Any?): Boolean = this.key == other
        override fun hashCode(): Int = key.hashCode()
    }

    class OptionWithValue<T>(
        override val key: String,
        override val nameId: Int,
        val value: T
    ): Option(key, nameId)

    open class UpdateChannelOptions(vararg options: OptionWithValue<UpdateParser>): MenuOptionsWithValues<UpdateParser>(options.toList()) {
        companion object {
            const val RELEASE = "Release"
            const val DEVELOPMENT = "Development"
        }
    }

    data object GitHubUpdateChannelOptions: UpdateChannelOptions(
        OptionWithValue(RELEASE, R.string.key_update_channel_release, GithubParser.ReleaseParser),
        OptionWithValue(DEVELOPMENT, R.string.key_update_channel_development, GithubParser.DevelopmentParser),
        OptionWithValue("CI", R.string.key_update_channel_ci, GithubParser.CIParser)
    )

    data object LnrAPIUpdateChannelOptions: UpdateChannelOptions(
        OptionWithValue(RELEASE, R.string.key_update_channel_release, APIParser.StableParser),
        OptionWithValue(DEVELOPMENT, R.string.key_update_channel_development, APIParser.BetaParser),
        OptionWithValue("CI", R.string.key_update_channel_ci, APIParser.UnstableParser)
    )

    data object UpdatePlatformOptions: MenuOptionsWithValues<UpdateChannelOptions>() {
        val GitHub = option("GitHub", R.string.key_platform_github, GitHubUpdateChannelOptions)
        val LnrAPI = option("LnrAPI", R.string.key_platform_lnr_api, LnrAPIUpdateChannelOptions)
    }

    data object DarkModeOptions: MenuOptions(
        Option("FollowSystem", R.string.key_dark_mode_follow_system),
        Option("Enabled", R.string.key_dark_mode_enabled),
        Option("Disabled", R.string.key_dark_mode_disabled)
    )

    data object AppLocaleOptions: MenuOptions(
        Option("none", R.string.key_locale_none),
        Option("zh-CN", R.string.key_locale_zh_cn),
        Option("zh-HK", R.string.key_locale_zh_hk),
        Option("zh-TW", R.string.key_locale_zh_tw),
        Option("ja-JP", R.string.key_locale_ja_jp),
        Option("ko-kr", R.string.key_locale_ko_kr),
        Option("ko-kp", R.string.key_locale_ko_kp)
    )

    data object LogLevelOptions: MenuOptions(
        Option("none", R.string.key_log_level_none),
        Option("error", R.string.key_log_level_error),
        Option("warning", R.string.key_log_level_warning),
        Option("info", R.string.key_log_level_info),
        Option("debug", R.string.key_log_level_debug),
        Option("verbose", R.string.key_log_level_verbose),
    )

    data object LightThemeNameOptions: MenuOptions(
        Option("light_default", R.string.key_light_theme_default),
        Option("light_designer", R.string.key_light_theme_designer)
    )

    data object DarkThemeNameOptions: MenuOptions(
        Option("dark_default", R.string.key_dark_theme_default),
        Option("dark_obsidian", R.string.key_dark_theme_obsidian),
        Option("dark_designer", R.string.key_dark_theme_designer)
    )

    data object ReaderBgImageDisplayModeOptions: MenuOptions() {
        val Fixed = option("fixed", R.string.key_bg_image_display_mode_fixed)
        val Loop = option("loop", R.string.key_bg_image_display_mode_loop)
    }

    data object FlipAnimationOptions: MenuOptions() {
        val None = option("none", R.string.key_flip_animation_none)
        val ScrollWithoutShadow = option("scroll", R.string.key_flip_animation_scroll)
    }

    data object SelectImage: MenuOptions() {
        val Default = option("default", R.string.key_default_image)
        val Customize = option("customize", R.string.key_customize_image)
    }

    data object SelectText: MenuOptions() {
        val Default = option("default", R.string.key_default_text)
        val Customize = option("customize", R.string.key_customize_text)
    }

    data object ReaderIndicatorBatteryDisplayMode: MenuOptions() {
        val Hidden = option("hidden", R.string.key_reader_indicator_battery_display_mode_hidden)
        val Classic = option("classic", R.string.key_reader_indicator_battery_display_mode_classic)
    }

    data object ReaderBackBlockMode: MenuOptions() {
        val None = option("none", R.string.key_reader_back_block_mode_none)
        val DoublePress = option("double_press", R.string.key_reader_back_block_mode_double_press)
        val FullyBlocked = option("blocked", R.string.key_reader_back_block_mode_blocked)
    }
    data object DateFormatOptions: MenuOptions() {
        val Numeric = option("numeric", R.string.key_date_format_numeric)
        val Written = option("written", R.string.key_date_format_written)
    }

    data object DateOrderOptions: MenuOptions() {
        val Auto = option("auto", R.string.key_date_order_auto)
        val YMD  = option("ymd",  R.string.key_date_order_ymd)
        val DMY  = option("dmy",  R.string.key_date_order_dmy)
        val MDY  = option("mdy",  R.string.key_date_order_mdy)
    }

    data object DurationStyleOptions: MenuOptions() {
        val Simple = option("simple", R.string.key_duration_style_simple)
        val Detailed = option("detailed", R.string.key_duration_style_detailed)
    }

    data object BookshelfSortTypeOptions: MenuOptionsWithValues<BookshelfSortType>(
        OptionWithValue(BookshelfSortType.Default.key, R.string.bookshelf_sort_default, BookshelfSortType.Default),
        OptionWithValue(BookshelfSortType.Latest.key, R.string.bookshelf_sort_latest, BookshelfSortType.Latest),
        OptionWithValue(BookshelfSortType.Name.key, R.string.bookshelf_sort_name, BookshelfSortType.Name),
        OptionWithValue(BookshelfSortType.WordCount.key, R.string.bookshelf_sort_word_count, BookshelfSortType.WordCount)
    )

}
