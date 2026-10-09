package com.example.innogeeks.feature_home.presentation.memberhome

sealed interface MemberHomeAction {
    data object OnRefresh : MemberHomeAction
    data object OnScreenShown : MemberHomeAction
    data object OnRetryClick : MemberHomeAction
    data object OnAttendanceClick : MemberHomeAction
    data object OnToMarkClick : MemberHomeAction
    data object OnAllResourcesClick : MemberHomeAction
    data class OnResourceClick(val url: String) : MemberHomeAction
}
