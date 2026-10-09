package com.example.innogeeks.feature_attendance.data.remote

import android.content.Context
import com.example.innogeeks.core.data.fake.FakeStore
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

class FakeAttendanceRemoteDataSource(context: Context) : AttendanceRemoteDataSource {

    private data class FakeMember(val id: String, val name: String, val role: String)
    @Serializable
    private data class FakeSession(val id: String, val title: String, val date: String)

    @Serializable
    private data class MarkEntry(val sessionId: String, val accountId: String, val status: String)

    @Serializable
    private data class Snapshot(val sessions: List<FakeSession>, val marks: List<MarkEntry>)

    private val store = FakeStore(context)
    private val loadLock = Mutex()
    private var loaded = false

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
    // Session indexes each member missed, varied so the roster's attendance sort has something to show.
    private val missedSessions = mapOf(
        MY_ID to setOf(0, 4),
        "m2" to emptySet(),
        "m3" to setOf(0, 1, 3),
        "m4" to setOf(2),
        "m5" to setOf(1, 3),
        "m6" to setOf(4),
        "m7" to setOf(0, 1, 2, 4),
        "c1" to emptySet()
    )
    private val sessions = mutableListOf<FakeSession>()
    private val marks = mutableMapOf<Pair<String, String>, String>() // (sessionId, accountId) -> status

    init {
        val dates = listOf("2026-09-02", "2026-09-09", "2026-09-16", "2026-09-23", "2026-09-30", "2026-10-07")
        val titles = listOf(
            "Kotlin crash course", "Compose layouts", "State & recomposition",
            "Navigation in Compose", "Room + DataStore", "Ktor & REST APIs"
        )
        dates.forEachIndexed { s, date ->
            val id = "s${s + 1}"
            sessions += FakeSession(id, titles[s], date)
            // The latest session is still unmarked, like a coordinator who hasn't got to it yet.
            if (s == dates.lastIndex) return@forEachIndexed
            members.forEach { member ->
                val absent = s in missedSessions.getValue(member.id)
                marks[id to member.id] = if (absent) "ABSENT" else "PRESENT"
            }
        }
    }

    // The seed above is the starting point; a saved copy from an earlier run replaces it, so demo edits survive a restart.
    private suspend fun ensureLoaded() = loadLock.withLock {
        if (loaded) return@withLock
        store.read(SNAPSHOT_FILE, Snapshot.serializer())?.let { snapshot ->
            sessions.clear()
            sessions += snapshot.sessions
            marks.clear()
            snapshot.marks.forEach { marks[it.sessionId to it.accountId] = it.status }
        }
        loaded = true
    }

    private suspend fun save() {
        store.write(
            SNAPSHOT_FILE, Snapshot.serializer(),
            Snapshot(sessions.toList(), marks.map { (key, status) -> MarkEntry(key.first, key.second, status) })
        )
    }

    override suspend fun getMyAttendance(): Result<MyAttendanceDto, DataError.Network> {
        ensureLoaded()
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
        ensureLoaded()
        delay(600)
        return Result.Success(SessionListDto(sessions.sortedByDescending { it.date }.map { it.toDto() }))
    }

    override suspend fun getSessionRoster(sessionId: String): Result<SessionRosterDto, DataError.Network> {
        ensureLoaded()
        delay(500)
        val session = sessions.firstOrNull { it.id == sessionId }
            ?: return Result.Error(DataError.Network.NOT_FOUND)
        return Result.Success(rosterOf(session))
    }

    override suspend fun createSession(body: CreateSessionRequestDto): Result<AttendanceSessionDto, ApiFailure> {
        ensureLoaded()
        delay(500)
        if (body.title.isBlank()) return Result.Error(ApiFailure.Api("VALIDATION_ERROR"))
        val session = FakeSession("s${sessions.size + 1}", body.title, body.date)
        sessions += session
        save()
        return Result.Success(session.toDto())
    }

    override suspend fun submitMarks(sessionId: String, body: SubmitMarksRequestDto): Result<SessionRosterDto, ApiFailure> {
        ensureLoaded()
        delay(700)
        val session = sessions.firstOrNull { it.id == sessionId }
            ?: return Result.Error(ApiFailure.Api("ATTENDANCE_SESSION_NOT_FOUND"))
        // Whole batch fails together if any account is outside the domain, like the real endpoint.
        if (body.marks.any { mark -> members.none { it.id == mark.accountId } }) {
            return Result.Error(ApiFailure.Api("ATTENDANCE_NOT_IN_DOMAIN"))
        }
        body.marks.forEach { marks[sessionId to it.accountId] = it.status }
        save()
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
        roster = members.map {
            RosterEntryDto(it.id, it.name, it.role, marks[session.id to it.id], percentExcluding(it.id, session.id))
        }
    )

    // Own attendance over every other session this person was marked in, like the real server.
    private fun percentExcluding(accountId: String, sessionId: String): Int? {
        val others = sessions.filter { it.id != sessionId }.mapNotNull { marks[it.id to accountId] }
        return if (others.isEmpty()) null else others.count { it == "PRESENT" } * 100 / others.size
    }

    private companion object {
        const val MY_ID = "me"
        const val SNAPSHOT_FILE = "fake_attendance.json"
    }
}
