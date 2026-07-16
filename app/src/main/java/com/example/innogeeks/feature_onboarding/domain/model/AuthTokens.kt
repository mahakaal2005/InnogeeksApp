package com.example.innogeeks.feature_onboarding.domain.model

// The auth token(s) returned on login. Saved in DataStore (Lesson 8) so the session
// survives restarts and drives auto-login. Just the access token for now — add a
// refreshToken field later only if the backend actually issues one.
data class AuthTokens(
    val accessToken : String
)
