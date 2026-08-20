package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.ui.graphics.Color

enum class Level(val color: Color, val colorWeekends: Color) {
    Zero(color = Color(0x16444444), colorWeekends = Color(0x16444444)),
    One(color = Color(0x33329c32), colorWeekends = Color(0x3329538f)),
    Two(color = Color(0x77329c32), colorWeekends = Color(0x7729538f)),
    Three(color = Color(0xAA329c32), colorWeekends = Color(0xAA29538f)),
    Four(color = Color(0xFF329c32), colorWeekends = Color(0xFF29538f)),
}
