package com.example.innogeeks.feature_attendance.data.repository

import com.example.innogeeks.core.domain.error.ApiFailure
import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.core.domain.util.mapData
import com.example.innogeeks.core.domain.util.mapError
import com.example.innogeeks.feature_attendance.data.mapper.toAttendanceSession
import com.example.innogeeks.feature_attendance.data.mapper.toMarkDto
import com.example.innogeeks.feature_attendance.data.mapper.toMyAttendance
import com.example.innogeeks.feature_attendance.data.mapper.toSessionRoster
import com.example.innogeeks.feature_attendance.data.remote.AttendanceRemoteDataSource
import com.example.innogeeks.feature_attendance.data.remote.dto.CreateSessionRequestDto
import com.example.innogeeks.feature_attendance.data.remote.dto.SubmitMarksRequestDto
import com.example.innogeeks.feature_attendance.domain.model.AttendanceError
import com.example.innogeeks.feature_attendance.domain.model.AttendanceFailure
import com.example.innogeeks.feature_attendance.domain.model.AttendanceMark
import com.example.innogeeks.feature_attendance.domain.model.AttendanceSession
import com.example.innogeeks.feature_attendance.domain.model.MyAttendance
import com.example.innogeeks.feature_attendance.domain.model.SessionRoster
import com.example.innogeeks.feature_attendance.domain.repository.AttendanceRepository
import kotlinx.datetime.LocalDate

class DefaultAttendanceRepository(
    private val remoteDataSource: AttendanceRemoteDataSource
) : AttendanceRepository {

    override suspend fun getMyAttendance(): Result<MyAttendance, DataError.Network> =
        remoteDataSource.getMyAttendance().mapData { it.toMyAttendance() }

    override suspend fun getDomainSessions(): Result<List<AttendanceSession>, DataError.Network> =
        remoteDataSource.getDomainSessions().mapData { list -> list.sessions.mapNotNull { it.toAttendanceSession() } }

    override suspend fun getSessionRoster(sessionId: String): Result<SessionRoster, DataError.Network> =
        remoteDataSource.getSessionRoster(sessionId).toRosterResult { DataError.Network.SERIALIZATION }

    override suspend fun createSession(title: String, date: LocalDate): Result<AttendanceSession, AttendanceFailure> =
        remoteDataSource.createSession(CreateSessionRequestDto(title, date.toString()))
            .mapError { it.toFailure() }
            .mapDataOrFail { it.toAttendanceSession() }

    override suspend fun submitMarks(sessionId: String, marks: List<AttendanceMark>): Result<SessionRoster, AttendanceFailure> =
        remoteDataSource.submitMarks(sessionId, SubmitMarksRequestDto(marks.map { it.toMarkDto() }))
            .mapError { it.toFailure() }
            .mapDataOrFail { it.toSessionRoster() }

    private fun ApiFailure.toFailure(): AttendanceFailure = when (this) {
        is ApiFailure.Api -> AttendanceFailure.Rejected(AttendanceError.fromCode(code))
        is ApiFailure.Transport -> AttendanceFailure.Transport(error)
    }

    // A reply the mapper rejects (bad date) is a parse problem, not a business error.
    private inline fun <T, R> Result<T, AttendanceFailure>.mapDataOrFail(map: (T) -> R?): Result<R, AttendanceFailure> =
        when (this) {
            is Result.Success -> map(data)?.let { Result.Success(it) }
                ?: Result.Error(AttendanceFailure.Transport(DataError.Network.SERIALIZATION))
            is Result.Error -> Result.Error(error)
        }

    private inline fun Result<com.example.innogeeks.feature_attendance.data.remote.dto.SessionRosterDto, DataError.Network>.toRosterResult(
        onInvalid: () -> DataError.Network
    ): Result<SessionRoster, DataError.Network> = when (this) {
        is Result.Success -> data.toSessionRoster()?.let { Result.Success(it) } ?: Result.Error(onInvalid())
        is Result.Error -> Result.Error(error)
    }
}
