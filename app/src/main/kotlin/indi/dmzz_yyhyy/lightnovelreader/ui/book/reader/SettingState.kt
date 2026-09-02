package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import indi.dmzz_yyhyy.lightnovelreader.data.setting.AbstractSettingState
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope

@Suppress("MemberVisibilityCanBePrivate")
class SettingState(
    userDataRepository: UserDataRepository,
    coroutineScope: CoroutineScope
) : AbstractSettingState(coroutineScope) {
    val fontSizeUserData = userDataRepository.floatUserData(UserDataPath.Reader.FontSize.path)
    val lineHeightUserData = userDataRepository.floatUserData(UserDataPath.Reader.LineHeight.path)
    val fontWeighUserData = userDataRepository.floatUserData(UserDataPath.Reader.FontWeigh.path)
    val spacingAfterParagraphUserData = userDataRepository.floatUserData(UserDataPath.Reader.SpacingAfterParagraph.path)
    val firstLineTextIndentUserData = userDataRepository.floatUserData(UserDataPath.Reader.FirstLineTextIndent.path)
    val keepScreenOnUserData =
        userDataRepository.booleanUserData(UserDataPath.Reader.KeepScreenOn.path)
    val enableHideStatusBarUserData =
        userDataRepository.booleanUserData(UserDataPath.Reader.EnableHideStatusBar.path)
    val enableBackgroundImageUserData =
        userDataRepository.booleanUserData(UserDataPath.Reader.EnableBackgroundImage.path)
    val backgroundImageDisplayModeUserData =
        userDataRepository.stringUserData(UserDataPath.Reader.BackgroundImageDisplayMode.path)
    val isUsingFlipPageUserData =
        userDataRepository.booleanUserData(UserDataPath.Reader.IsUsingFlipPage.path)
    val isUsingClickFlipPageUserData =
        userDataRepository.booleanUserData(UserDataPath.Reader.IsUsingClickFlipPage.path)
    val isUsingVolumeKeyFlipUserData =
        userDataRepository.booleanUserData(UserDataPath.Reader.IsUsingVolumeKeyFlip.path)
    val volumeKeyContinuousFlipIntervalUserData =
        userDataRepository.floatUserData(UserDataPath.Reader.VolumeKeyContinuousFlipInterval.path)
    val flipAnimeUserData = userDataRepository.stringUserData(UserDataPath.Reader.FlipAnime.path)
    val batteryIndicatorDisplayModeUserData =
        userDataRepository.stringUserData(UserDataPath.Reader.BatteryIndicatorDisplayMode.path)
    val enableTimeIndicatorUserData =
        userDataRepository.booleanUserData(UserDataPath.Reader.EnableTimeIndicator.path)
    val enableChapterTitleIndicatorUserData = userDataRepository.booleanUserData(
        UserDataPath.Reader.EnableChapterTitleIndicator.path
    )
    val enableReadingChapterProgressIndicatorUserData = userDataRepository.booleanUserData(
        UserDataPath.Reader.EnableReadingChapterProgressIndicator.path
    )
    val autoPaddingUserData =
        userDataRepository.booleanUserData(UserDataPath.Reader.AutoPadding.path)
    val topPaddingUserData = userDataRepository.floatUserData(UserDataPath.Reader.TopPadding.path)
    val bottomPaddingUserData =
        userDataRepository.floatUserData(UserDataPath.Reader.BottomPadding.path)
    val leftPaddingUserData = userDataRepository.floatUserData(UserDataPath.Reader.LeftPadding.path)
    val rightPaddingUserData =
        userDataRepository.floatUserData(UserDataPath.Reader.RightPadding.path)
    val textColorUserData = userDataRepository.colorUserData(UserDataPath.Reader.TextColor.path)
    val textDarkColorUserData =
        userDataRepository.colorUserData(UserDataPath.Reader.TextDarkColor.path)
    val fontUriUserData = userDataRepository.uriUserData(UserDataPath.Reader.FontUri.path)
    val backgroundColorUserData =
        userDataRepository.colorUserData(UserDataPath.Reader.BackgroundColor.path)
    val backgroundDarkColorUserData =
        userDataRepository.colorUserData(UserDataPath.Reader.BackgroundDarkColor.path)
    val backgroundImageUriUserData =
        userDataRepository.uriUserData(UserDataPath.Reader.BackgroundImageUri.path)
    val backgroundDarkImageUriUserData =
        userDataRepository.uriUserData(UserDataPath.Reader.BackgroundDarkImageUri.path)
    val backBlockModeUserData =
        userDataRepository.stringUserData(UserDataPath.Reader.BackBlockMode.path)
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