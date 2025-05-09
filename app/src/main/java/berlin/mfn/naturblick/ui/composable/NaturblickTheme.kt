/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import berlin.mfn.naturblick.R


data class NaturblickColors(
    val primary: Color,
    val primaryVariant: Color,
    val onPrimary: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val onSecondary: Color,
    val surface: Color,
    val onSurface: Color,
    val tertiary: Color,
    val feature: Color,
    val backdrop: Color,
    val onPrimaryHighEmphasis: Color,
    val onPrimaryMediumEmphasis: Color,
    val onPrimaryLowEmphasis: Color,
    val onPrimaryMinimumEmphasis: Color,
    val onPrimaryDisabled: Color,
    val onPrimarySignalLow: Color,
    val onPrimarySignalHigh: Color,
    val onPrimaryButtonPrimary: Color,
    val onPrimaryButtonSecondary: Color,
    val onPrimaryTag: Color,
    val onPrimaryStableBright: Color,
    val onPrimaryStableDark: Color,
    val onSecondaryHighEmphasis: Color,
    val onSecondaryMediumEmphasis: Color,
    val onSecondaryLowEmphasis: Color,
    val onSecondaryMinimumEmphasis: Color,
    val onSecondaryDisabled: Color,
    val onSecondarySignalLow: Color,
    val onSecondarySignalHigh: Color,
    val onSecondaryButtonPrimary: Color,
    val onSecondaryButtonSecondary: Color,
    val onSecondaryTag: Color,
    val onSecondaryWarning: Color,
    val onSecondarySignalMedium: Color,
    val onSecondaryStableBright: Color,
    val onSecondaryStableDark: Color,
    val onFeatureHighEmphasis: Color,
    val onFeatureMediumEmphasis: Color,
    val onFeatureLowEmphasis: Color,
    val onFeatureDisabled: Color,
    val onFeatureSignalHigh: Color,
    val onFeatureSignalLow: Color,
    val onFeatureButtonPrimary: Color,
    val onFeatureButtonSecondary: Color,
    val onFeatureTag: Color
) {

}

data class NaturblickTypography(
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val subtitle1: TextStyle,
    val subtitle2: TextStyle,
    val subtitle3: TextStyle,
    val synonym: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val button: TextStyle,
    val caption: TextStyle,
    val overline: TextStyle,
)

object NaturblickTheme {
    private val Lato = Font(R.font.lato)
    private val LatoBlack = Font(R.font.lato_black, weight = FontWeight.Black)
    private val LatoItalic = Font(R.font.lato_italic, style = FontStyle.Italic)
    val FamilyLato = FontFamily(Lato, LatoBlack, LatoItalic)

    val colors: NaturblickColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography = NaturblickTypography(
        h2 = TextStyle(fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.Black),
        h3 = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black),
        h4 = TextStyle(fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.Black),
        h5 = TextStyle(fontSize = 22.sp, lineHeight = 22.sp, fontWeight = FontWeight.Black),
        h6 = TextStyle(fontSize = 19.sp, lineHeight = 2.sp, fontWeight = FontWeight.Black),
        subtitle1 = TextStyle(
            fontSize = 19.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.ExtraBold
        ),
        subtitle2 = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium
        ),
        subtitle3 = TextStyle(
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            letterSpacing = 0.25.sp
        ),
        synonym = TextStyle(
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.25.sp
        ),
        body1 = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
        body2 = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
        button = TextStyle(
            fontSize = 14.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.25.sp
        ),
        caption = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.4.sp
        ),
        overline = TextStyle(
            fontSize = 12.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            letterSpacing = 0.5.sp
        ),
    )

}
@Composable
fun NaturblickTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val naturblickColors = if (darkTheme) {
        NaturblickColors(
            primary = colorResource(R.color.sleep_500),
            primaryVariant = colorResource(R.color.primary_700),
            onPrimary = colorResource(R.color.base_150),
            secondary = colorResource(R.color.sleep_800),
            secondaryVariant = colorResource(R.color.secondary_500),
            onSecondary = colorResource(R.color.base_110),
            surface = colorResource(R.color.white),
            onSurface = colorResource(R.color.on_surface),
            tertiary = colorResource(R.color.relax_960),
            feature = colorResource(R.color.sleep_500),
            backdrop = colorResource(R.color.relax_960),
            onPrimaryHighEmphasis = colorResource(R.color.base_100),
            onPrimaryMediumEmphasis = colorResource(R.color.base_150),
            onPrimaryLowEmphasis = colorResource(R.color.base_170),
            onPrimaryMinimumEmphasis = colorResource(R.color.base_110),
            onPrimaryDisabled = colorResource(R.color.sleep_200),
            onPrimarySignalLow = colorResource(R.color.sleep_100),
            onPrimarySignalHigh = colorResource(R.color.signal_fun),
            onPrimaryButtonPrimary = colorResource(R.color.relax_200),
            onPrimaryButtonSecondary = colorResource(R.color.sleep_700),
            onPrimaryTag = colorResource(R.color.base_110),
            onPrimaryStableBright = colorResource(R.color.base_100),
            onPrimaryStableDark = colorResource(R.color.sleep_500),
            onSecondaryHighEmphasis = colorResource(R.color.base_100),
            onSecondaryMediumEmphasis = colorResource(R.color.base_110),
            onSecondaryLowEmphasis = colorResource(R.color.base_150),
            onSecondaryMinimumEmphasis = colorResource(R.color.sleep_600),
            onSecondaryDisabled = colorResource(R.color.base_150),
            onSecondarySignalLow = colorResource(R.color.sleep_100),
            onSecondarySignalHigh = colorResource(R.color.signal_fun),
            onSecondaryButtonPrimary = colorResource(R.color.base_100),
            onSecondaryButtonSecondary = colorResource(R.color.relax_200),
            onSecondaryTag = colorResource(R.color.base_110),
            onSecondaryWarning = colorResource(R.color.signal_pain),
            onSecondarySignalMedium = colorResource(R.color.signal_peace),
            onSecondaryStableBright = colorResource(R.color.base_100),
            onSecondaryStableDark = colorResource(R.color.sleep_500),
            onFeatureHighEmphasis = colorResource(R.color.base_100),
            onFeatureMediumEmphasis = colorResource(R.color.base_110),
            onFeatureLowEmphasis = colorResource(R.color.base_150),
            onFeatureDisabled = colorResource(R.color.base_150),
            onFeatureSignalHigh = colorResource(R.color.signal_dark_fun),
            onFeatureSignalLow = colorResource(R.color.sleep_100),
            onFeatureButtonPrimary = colorResource(R.color.base_100),
            onFeatureButtonSecondary = colorResource(R.color.sleep_100),
            onFeatureTag = colorResource(R.color.base_150)
        )
    } else {
        NaturblickColors(
            primary = colorResource(R.color.sleep_500),
            primaryVariant = colorResource(R.color.primary_700),
            onPrimary = colorResource(R.color.base_170),
            secondary = colorResource(R.color.base_100),
            secondaryVariant = colorResource(R.color.secondary_500),
            onSecondary = colorResource(R.color.sleep_500),
            surface = colorResource(R.color.white),
            onSurface = colorResource(R.color.on_surface),
            tertiary = colorResource(R.color.base_300),
            feature = colorResource(R.color.sleep_50),
            backdrop = colorResource(R.color.base_950),
            onPrimaryHighEmphasis = colorResource(R.color.base_100),
            onPrimaryMediumEmphasis = colorResource(R.color.base_110),
            onPrimaryLowEmphasis = colorResource(R.color.base_150),
            onPrimaryMinimumEmphasis = colorResource(R.color.base_170),
            onPrimaryDisabled = colorResource(R.color.base_150),
            onPrimarySignalLow = colorResource(R.color.sleep_100),
            onPrimarySignalHigh = colorResource(R.color.signal_fun),
            onPrimaryButtonPrimary = colorResource(R.color.relax_200),
            onPrimaryButtonSecondary = colorResource(R.color.sleep_700),
            onPrimaryTag = colorResource(R.color.base_150),
            onPrimaryStableBright = colorResource(R.color.base_100),
            onPrimaryStableDark = colorResource(R.color.sleep_500),
            onSecondaryHighEmphasis = colorResource(R.color.sleep_600),
            onSecondaryMediumEmphasis = colorResource(R.color.sleep_500),
            onSecondaryLowEmphasis = colorResource(R.color.sleep_400),
            onSecondaryMinimumEmphasis = colorResource(R.color.sleep_50),
            onSecondaryDisabled = colorResource(R.color.base_970),
            onSecondarySignalLow = colorResource(R.color.sleep_400),
            onSecondarySignalHigh = colorResource(R.color.signal_happy),
            onSecondaryButtonPrimary = colorResource(R.color.relax_200),
            onSecondaryButtonSecondary = colorResource(R.color.relax_200),
            onSecondaryTag = colorResource(R.color.relax_200),
            onSecondaryWarning = colorResource(R.color.signal_pain),
            onSecondarySignalMedium = colorResource(R.color.signal_peace),
            onSecondaryStableBright = colorResource(R.color.base_100),
            onSecondaryStableDark = colorResource(R.color.sleep_500),
            onFeatureHighEmphasis = colorResource(R.color.sleep_600),
            onFeatureMediumEmphasis = colorResource(R.color.sleep_500),
            onFeatureLowEmphasis = colorResource(R.color.sleep_400),
            onFeatureDisabled = colorResource(R.color.base_970),
            onFeatureSignalHigh = colorResource(R.color.signal_happy),
            onFeatureSignalLow = colorResource(R.color.sleep_300),
            onFeatureButtonPrimary = colorResource(R.color.relax_300),
            onFeatureButtonSecondary = colorResource(R.color.base_800),
            onFeatureTag = colorResource(R.color.relax_200),
        )
    }
    CompositionLocalProvider(LocalColors provides naturblickColors) {
        MaterialTheme(
            colors = if (darkTheme) darkColors(
                primary = naturblickColors.primary,
                onPrimary = naturblickColors.onPrimary,
                background = naturblickColors.secondary,
                onBackground = naturblickColors.onSecondaryMediumEmphasis,
            ) else lightColors(
                primary = naturblickColors.primary,
                onPrimary = naturblickColors.onPrimary,
                background = naturblickColors.secondary,
                onBackground = naturblickColors.onSecondaryMediumEmphasis,
            ),
            typography = Typography(
                defaultFontFamily = NaturblickTheme.FamilyLato,
                h2 = NaturblickTheme.typography.h2,
                h3 = NaturblickTheme.typography.h3,
                h4 = NaturblickTheme.typography.h4,
                h5 = NaturblickTheme.typography.h5,
                h6 = NaturblickTheme.typography.h6,
                subtitle1 = NaturblickTheme.typography.subtitle1,
                subtitle2 = NaturblickTheme.typography.subtitle2,
                body1 = NaturblickTheme.typography.body1,
                body2 = NaturblickTheme.typography.body2,
                button = NaturblickTheme.typography.button,
                caption = NaturblickTheme.typography.caption,
                overline = NaturblickTheme.typography.overline
            ),
            content = content
        )
    }
}


val LocalColors: ProvidableCompositionLocal<NaturblickColors> = compositionLocalOf {
    throw NotImplementedError()
}
