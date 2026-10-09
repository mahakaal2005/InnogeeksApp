package com.example.innogeeks.core.domain.attendance

import kotlinx.datetime.LocalDate

enum class SessionMark { PRESENT, ABSENT }

// mark is null until the coordinator marks that session.
data class OverviewSession(
    val id: String,
    val title: String,
    val date: LocalDate,
    val mark: SessionMark?
)

data class AttendanceOverview(
    val percent: Int, // over marked sessions only
    val present: Int,
    val marked: Int,
    val sessions: List<OverviewSession> // newest first
)
