package com.example.innogeeks.core.common

// App-wide, non-sensitive constants; environment config like BASE_URL flows through local.properties instead.

object Constants {
    val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
}