package com.example.innogeeks.core.common

// App-wide, non-sensitive constants that are the same in every build.
// (Environment/secret config like BASE_URL does NOT go here — it flows through
// local.properties -> BuildConfig instead. See app/build.gradle.kts.)

object Constants {
    val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
}