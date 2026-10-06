package com.example.innogeeks.feature_recruitment.presentation.tracker

sealed interface TrackerEvent {
    data object NavigateToResources : TrackerEvent
}
