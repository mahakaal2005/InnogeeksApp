package com.example.innogeeks.feature_onboarding.presentation.intro

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.example.innogeeks.R

// One intro slide's content. Immutable so Compose can skip recomposition safely.
@Immutable
data class IntroPage(
    @DrawableRes val imageRes: Int,
    val title: String,
    val subtitle: String
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
        title = "Build the Future",
        subtitle = "Work on real projects with the official technical club of KIET."
    ),
    IntroPage(
        imageRes = R.drawable.network,
        title = "Learn From Seniors",
        subtitle = "Join hackathons, bootcamps, and mentoring sessions across every domain."
    ),
    IntroPage(
        imageRes = R.drawable.rocket,
        title = "Launch Your Career",
        subtitle = "Grow your skills, ship your ideas, and stand out. We teach, we learn, we conquer."
    )
)
