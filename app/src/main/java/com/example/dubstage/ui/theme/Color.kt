package com.example.dubstage.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Default Studio Color Constants
val BgTop = Color(0xFF0F1115)
val BgBot = Color(0xFF0F1115)
val Panel = Color(0xFF1A1C1E)
val PanelHi = Color(0xFF25282C)
val Edge = Color(0xFF2D2F31)
val EdgeHi = Color(0xFF3E4759)
val Txt = Color(0xFFE2E2E6)
val TxtBright = Color(0xFFFFFFFF)
val Dim = Color(0xFF8E9199)
val DimHi = Color(0xFFC4C6D0)
val Acc = Color(0xFFA8C7FA)
val AccHi = Color(0xFFD1E4FF)
val AccDark = Color(0xFF004A77)
val AccDarker = Color(0xFF003355)
val AccDarkest = Color(0xFF001D35)
val Teal = Color(0xFF4EE5BB)
val TealHi = Color(0xFF7FF8D3)
val Red = Color(0xFFFF5449)
val RedHi = Color(0xFFFFB4AB)
val Gold = Color(0xFFFFDF99)
val GoldHi = Color(0xFFFFEFA6)
val SurfaceDark = Color(0xFF111318)
val Purple = Color(0xFFEADDFF)
val PurpleContainer = Color(0xFF4F378B)
val Green = Color(0xFFB5CCBA)
val GreenContainer = Color(0xFF374933)
val Ban = Color(0xFF004A77)
val BanTxt = Color(0xFFD1E4FF)
val WaveOrig = Color(0xFF283142)
val WaveOrigTxt = Color(0xFF6A7996)

data class DubStageColors(
    val bgTop: Color,
    val bgBot: Color,
    val panel: Color,
    val panelHi: Color,
    val edge: Color,
    val edgeHi: Color,
    val txt: Color,
    val txtBright: Color,
    val dim: Color,
    val dimHi: Color,
    val acc: Color,
    val accHi: Color,
    val accDark: Color,
    val accDarker: Color,
    val accDarkest: Color,
    val teal: Color,
    val tealHi: Color,
    val red: Color,
    val redHi: Color,
    val gold: Color,
    val goldHi: Color,
    val surfaceDark: Color
)

val DubStageDarkPalette = DubStageColors(
    bgTop = Color(0xFF0F1115),
    bgBot = Color(0xFF0F1115),
    panel = Color(0xFF1A1C1E),
    panelHi = Color(0xFF25282C),
    edge = Color(0xFF2D2F31),
    edgeHi = Color(0xFF3E4759),
    txt = Color(0xFFE2E2E6),
    txtBright = Color(0xFFFFFFFF),
    dim = Color(0xFF8E9199),
    dimHi = Color(0xFFC4C6D0),
    acc = Color(0xFFA8C7FA),
    accHi = Color(0xFFD1E4FF),
    accDark = Color(0xFF004A77),
    accDarker = Color(0xFF003355),
    accDarkest = Color(0xFF001D35),
    teal = Color(0xFF4EE5BB),
    tealHi = Color(0xFF7FF8D3),
    red = Color(0xFFFF5449),
    redHi = Color(0xFFFFB4AB),
    gold = Color(0xFFFFDF99),
    goldHi = Color(0xFFFFEFA6),
    surfaceDark = Color(0xFF111318)
)

val DubStageLightPalette = DubStageColors(
    bgTop = Color(0xFFF3F5F9),
    bgBot = Color(0xFFE8ECF2),
    panel = Color(0xFFFFFFFF),
    panelHi = Color(0xFFF0F3F8),
    edge = Color(0xFFD5DAE5),
    edgeHi = Color(0xFFB5BDCE),
    txt = Color(0xFF14171C),
    txtBright = Color(0xFF05070A),
    dim = Color(0xFF5B6271),
    dimHi = Color(0xFF3B414F),
    acc = Color(0xFF005DB4),
    accHi = Color(0xFF004488),
    accDark = Color(0xFFD2E4FF),
    accDarker = Color(0xFFB5D4FF),
    accDarkest = Color(0xFFFFFFFF),
    teal = Color(0xFF00897B),
    tealHi = Color(0xFF00695C),
    red = Color(0xFFBA1A1A),
    redHi = Color(0xFFFFDAD6),
    gold = Color(0xFF8A6200),
    goldHi = Color(0xFFFFDF99),
    surfaceDark = Color(0xFFF7F8FB)
)

val LocalDubStageColors = staticCompositionLocalOf { DubStageDarkPalette }

object DubStageTheme {
    val colors: DubStageColors
        @Composable
        @ReadOnlyComposable
        get() = LocalDubStageColors.current
}
