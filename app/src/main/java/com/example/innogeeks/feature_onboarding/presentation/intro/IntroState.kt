package com.example.innogeeks.feature_onboarding.presentation.intro

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import edu.kiet.innogeeks.R
import androidx.annotation.StringRes

// One intro slide's content. Immutable so Compose can skip recomposition safely.
@Immutable
data class IntroPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int
)

// The intro carousel's state. pages is the fixed content; currentPage tracks which slide is
// showing so the ViewModel knows when we're on the LAST one (to show "Get Started").
@Immutable
data class IntroState(
    val pages: List<IntroPage> = defaultIntroPages,
    val currentPage: Int = 0
) {
    val isLastPage: Boolean get() = currentPage == pages.lastIndex
}

// Copy drawn from the club mockups / site. First-launch users see these once.
val defaultIntroPages = listOf(
    IntroPage(
        imageRes = R.drawable.microchip,
        titleRes = R.string.intro_build_future_title,
        subtitleRes = R.string.intro_build_future_subtitle
    ),
    IntroPage(
        imageRes = R.drawable.network,
        titleRes = R.string.intro_learn_seniors_title,
        subtitleRes = R.string.intro_learn_seniors_subtitle
    ),
    IntroPage(
        imageRes = R.drawable.rocket,
        titleRes = R.string.intro_launch_career_title,
        subtitleRes = R.string.intro_launch_career_subtitle
    )
)
