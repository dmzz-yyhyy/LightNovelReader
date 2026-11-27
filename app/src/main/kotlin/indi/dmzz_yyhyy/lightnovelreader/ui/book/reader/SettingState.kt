package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import indi.dmzz_yyhyy.lightnovelreader.data.setting.AbstractSettingState
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope

@Suppress("MemberVisibilityCanBePrivate")
class SettingState(
    userDataRepository: UserDataRepository,
    coroutineScope: CoroutineScope
) : AbstractSettingState(coroutineScope) {
    val writeModeUserData = userDataRepository.userData(UserDataPath.Reader.WriteMode)
    val writeMode by writeModeUserData.safeAsState(WriteMode.Horizontal.name)
    val fontSizeUserData = userDataRepository.userData(UserDataPath.Reader.FontSize)
    val lineHeightUserData = userDataRepository.userData(UserDataPath.Reader.LineHeight)
    val fontWeighUserData = userDataRepository.userData(UserDataPath.Reader.FontWeigh)
    val spacingAfterParagraphUserData =
        userDataRepository.userData(UserDataPath.Reader.SpacingAfterParagraph)
    val firstLineTextIndentUserData =
        userDataRepository.userData(UserDataPath.Reader.FirstLineTextIndent)
    val keepScreenOnUserData =
        userDataRepository.userData(UserDataPath.Reader.KeepScreenOn)
    val enableHideStatusBarUserData =
        userDataRepository.userData(UserDataPath.Reader.EnableHideStatusBar)
    val enableBackgroundImageUserData =
        userDataRepository.userData(UserDataPath.Reader.EnableBackgroundImage)
    val backgroundImageDisplayModeUserData =
        userDataRepository.userData(UserDataPath.Reader.BackgroundImageDisplayMode)
    val isUsingFlipPageUserData =
        userDataRepository.userData(UserDataPath.Reader.IsUsingFlipPage)
    val isUsingClickFlipPageUserData =
        userDataRepository.userData(UserDataPath.Reader.IsUsingClickFlipPage)
    val isUsingVolumeKeyFlipUserData =
        userDataRepository.userData(UserDataPath.Reader.IsUsingVolumeKeyFlip)
    val volumeKeyContinuousFlipIntervalUserData =
        userDataRepository.userData(UserDataPath.Reader.VolumeKeyContinuousFlipInterval)
    val flipAnimeUserData = userDataRepository.userData(UserDataPath.Reader.FlipAnime)
    val batteryIndicatorDisplayModeUserData =
        userDataRepository.userData(UserDataPath.Reader.BatteryIndicatorDisplayMode)
    val enableTimeIndicatorUserData =
        userDataRepository.userData(UserDataPath.Reader.EnableTimeIndicator)
    val enableChapterTitleIndicatorUserData =
        userDataRepository.userData(UserDataPath.Reader.EnableChapterTitleIndicator)
    val enableReadingChapterProgressIndicatorUserData =
        userDataRepository.userData(UserDataPath.Reader.EnableReadingChapterProgressIndicator)
    val autoPaddingUserData =
        userDataRepository.userData(UserDataPath.Reader.AutoPadding)
    val topPaddingUserData = userDataRepository.userData(UserDataPath.Reader.TopPadding)
    val bottomPaddingUserData =
        userDataRepository.userData(UserDataPath.Reader.BottomPadding)
    val leftPaddingUserData = userDataRepository.userData(UserDataPath.Reader.LeftPadding)
    val rightPaddingUserData =
        userDataRepository.userData(UserDataPath.Reader.RightPadding)
    val textColorUserData = userDataRepository.userData(UserDataPath.Reader.TextColor)
    val textDarkColorUserData =
        userDataRepository.userData(UserDataPath.Reader.TextDarkColor)
    val fontUriUserData = userDataRepository.userData(UserDataPath.Reader.FontUri)
    val backgroundColorUserData =
        userDataRepository.userData(UserDataPath.Reader.BackgroundColor)
    val backgroundDarkColorUserData =
        userDataRepository.userData(UserDataPath.Reader.BackgroundDarkColor)
    val backgroundImageUriUserData =
        userDataRepository.userData(UserDataPath.Reader.BackgroundImageUri)
    val backgroundDarkImageUriUserData =
        userDataRepository.userData(UserDataPath.Reader.BackgroundDarkImageUri)
    val backBlockModeUserData =
        userDataRepository.userData(UserDataPath.Reader.BackBlockMode)
    private val readerStyle = ReaderStyle()

    val fontSize by fontSizeUserData.safeAsState(readerStyle.fontSize.value)
    val lineHeight by lineHeightUserData.safeAsState(readerStyle.lineHeight.value)
    val fontWeigh by fontWeighUserData.safeAsState(readerStyle.fontWeight.weight.toFloat())
    val spacingAfterParagraph by spacingAfterParagraphUserData.safeAsState(readerStyle.spacingAfterParagraph.value)
    val firstLineTextIndent by firstLineTextIndentUserData.safeAsState(readerStyle.textIndent.firstLine.value)
    val keepScreenOn by keepScreenOnUserData.safeAsState(false)
    val enableHideStatusBar by enableHideStatusBarUserData.safeAsState(true)
    val enableBackgroundImage by enableBackgroundImageUserData.safeAsState(false)
    val backgroundImageDisplayMode by backgroundImageDisplayModeUserData.safeAsState("fixed")
    val isUsingFlipPage by isUsingFlipPageUserData.safeAsState(false)
    val isUsingClickFlipPage by isUsingClickFlipPageUserData.safeAsState(false)
    val isUsingVolumeKeyFlip by isUsingVolumeKeyFlipUserData.safeAsState(false)
    val volumeKeyContinuousFlipInterval by volumeKeyContinuousFlipIntervalUserData.safeAsState(-1f)
    val flipAnime by flipAnimeUserData.safeAsState(MenuOptions.FlipAnimationOptions.ScrollWithoutShadow)
    val batteryIndicatorDisplayMode by batteryIndicatorDisplayModeUserData.safeAsState("classic")
    val enableTimeIndicator by enableTimeIndicatorUserData.safeAsState(true)
    val enableChapterTitleIndicator by enableChapterTitleIndicatorUserData.safeAsState(true)
    val enableReadingChapterProgressIndicator by enableReadingChapterProgressIndicatorUserData.safeAsState(
        true
    )
    val autoPadding by autoPaddingUserData.safeAsState(true)
    val topPadding by topPaddingUserData.safeAsState(12f)
    val bottomPadding by bottomPaddingUserData.safeAsState(12f)
    val leftPadding by leftPaddingUserData.safeAsState(16f)
    val rightPadding by rightPaddingUserData.safeAsState(16f)
    val textColor by textColorUserData.safeAsState(Color.Unspecified)
    val textDarkColor by textDarkColorUserData.safeAsState(Color.Unspecified)
    val fontUri by fontUriUserData.safeAsState(Uri.EMPTY)
    val backgroundColor by backgroundColorUserData.safeAsState(Color.Unspecified)
    val backgroundDarkColor by backgroundDarkColorUserData.safeAsState(Color.Unspecified)
    val backgroundImageUri by backgroundImageUriUserData.safeAsState(Uri.EMPTY)
    val backgroundDarkImageUri by backgroundDarkImageUriUserData.safeAsState(Uri.EMPTY)
    val backBlockMode by backBlockModeUserData.safeAsState("none")
}
