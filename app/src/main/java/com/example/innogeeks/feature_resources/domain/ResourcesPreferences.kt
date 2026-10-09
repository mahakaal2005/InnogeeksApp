package com.example.innogeeks.feature_resources.domain

import kotlinx.coroutines.flow.Flow

interface ResourcesPreferences {
    // True once a coordinator has dismissed the swipe/hold tip or used a gesture.
    val tipSeen: Flow<Boolean>
    suspend fun markTipSeen()
}
