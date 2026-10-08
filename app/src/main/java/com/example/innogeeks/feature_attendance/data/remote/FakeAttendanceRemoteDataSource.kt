package com.example.innogeeks.feature_attendance.data.remote

import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_attendance.data.remote.dto.AttendanceRecordDto
import com.example.innogeeks.feature_attendance.data.remote.dto.AttendanceSessionDto
import com.example.innogeeks.feature_attendance.data.remote.dto.AttendanceSummaryDto
import com.example.innogeeks.feature_attendance.data.remote.dto.CreateSessionRequestDto
import com.example.innogeeks.feature_attendance.data.remote.dto.MyAttendanceDto
import com.example.innogeeks.feature_attendance.data.remote.dto.RosterEntryDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionHeaderDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionListDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SessionRosterDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SubmitMarksRequestDto
import kotlinx.coroutines.delay

class FakeAttendanceRemoteDataSource : AttendanceRemoteDataSource {

    private data class FakeMember(val id: String, val name: String, val role: String)
    private data class FakeSession(val id: String, val title: String, val date: String)

    // In-memory state so a dev run behaves like the server: marking "me" changes my percent.
    private val members = listOf(
        FakeMember(MY_ID, "You", "MEMBER"),
        FakeMember("m2", "Asha Verma", "MEMBER"),
        FakeMember("m3", "Rohan Gupta", "MEMBER"),
        FakeMember("m4", "Neha Singh", "MEMBER"),
        FakeMember("m5", "Karan Mehta", "MEMBER"),
        FakeMember("m6", "Isha Rao", "MEMBER"),
        FakeMember("m7", "Dev Patel", "MEMBER"),
        FakeMember("c1", "Priya Nair", "COORDINATOR")
    )
    private val sessions = mutableListOf<FakeSession>()
    private val marks = mutableMapOf<Pair<String, String>, String>() // (sessionId, accountId) -> status

    init {
        val dates = listOf("2026-09-02", "2026-09-09", "2026-09-16", "2026-09-23", "2026-09-30", "2026-10-07")
        dates.forEachIndexed { s, date ->
            val id = "s${s + 1}"
            sessions += FakeSession(id, "Weekly sync #${s + 1}", date)
            // The latest session is still unmarked, like a coordinator who hasn't got to it yet.
            if (s == dates.lastIndex) return@forEachIndexed
            members.forEachIndexed { m, member ->
                // Deterministic mix: every member misses a different session or two.
                val absent = (s + m) % 4 == 0
                marks[id to member.id] = if (absent) "ABSENT" else "PRESENT"
            }
        }
    }

    override suspend fun getMyAttendance(): Result<MyAttendanceDto, DataError.Network> {
        delay(600)
        val records = sessions.sortedByDescending { it.date }.map { session ->
            AttendanceRecordDto(session.id, session.title, session.date, marks[session.id to MY_ID])
        }
        // The summary counts marked sessions only, like the real server.
        val marked = records.count { it.status != null }
        val present = records.count { it.status == "PRESENT" }
        val percent = if (marked == 0) 0 else present * 100 / marked
        return Result.Success(MyAttendanceDto(AttendanceSummaryDto(marked, present, percent), records))
    }

    override suspend fun getDomainSessions(): Result<SessionListDto, DataError.Network> {
        delay(600)
        return Result.Success(SessionListDto(sessions.sortedByDescending { it.date }.map { it.toDto() }))
    }

    override suspend fun getSessionRoster(sessionId: String): Result<SessionRosterDto, DataError.Network> {
        delay(500)
        val session = sessions.firstOrNull { it.id == sessionId }
            ?: return Result.Error(DataError.Network.NOT_FOUND)
        return Result.Success(rosterOf(session))
    }

    override suspend fun createSession(body: CreateSessionRequestDto): Result<AttendanceSessionDto, ApiFailure> {
        delay(500)
        if (body.title.isBlank()) return Result.Error(ApiFailure.Api("VALIDATION_ERROR"))
        val session = FakeSession("s${sessions.size + 1}", body.title, body.date)
        sessions += session
        return Result.Success(session.toDto())
    }

    override suspend fun submitMarks(sessionId: String, body: SubmitMarksRequestDto): Result<SessionRosterDto, ApiFailure> {
        delay(700)
        val session = sessions.firstOrNull { it.id == sessionId }
            ?: return Result.Error(ApiFailure.Api("ATTENDANCE_SESSION_NOT_FOUND"))
        // Whole batch fails together if any account is outside the domain, like the real endpoint.
        if (body.marks.any { mark -> members.none { it.id == mark.accountId } }) {
            return Result.Error(ApiFailure.Api("ATTENDANCE_NOT_IN_DOMAIN"))
        }
        body.marks.forEach { marks[sessionId to it.accountId] = it.status }
        return Result.Success(rosterOf(session))
    }

    private fun FakeSession.toDto() = AttendanceSessionDto(
        id = id,
        title = title,
        date = date,
        markedCount = members.count { marks.containsKey(this.id to it.id) },
        memberCount = members.size
    )

    private fun rosterOf(session: FakeSession) = SessionRosterDto(
        session = SessionHeaderDto(session.id, session.title, session.date),
        roster = members.map { RosterEntryDto(it.id, it.name, it.role, marks[session.id to it.id]) }
    )

    private companion object {
        const val MY_ID = "me"
    }
}
