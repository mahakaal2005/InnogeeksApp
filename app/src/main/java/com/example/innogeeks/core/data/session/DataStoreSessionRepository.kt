package com.example.innogeeks.core.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.domain.session.Session
import com.example.innogeeks.core.domain.session.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// One DataStore instance per process, hung off Context as the DataStore docs require.
private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "innogeeks_session"
)

class DataStoreSessionRepository(context: Context) : SessionRepository {

    private val dataStore = context.sessionDataStore

    override val session: Flow<Session> = dataStore.data.map { prefs ->
        val token = prefs[ACCESS_TOKEN]
        val email = prefs[COLLEGE_EMAIL]
        // A token with no email is corrupt state, so both are required to count as signed in.
        if (token.isNullOrBlank() || email.isNullOrBlank()) {
            Session.Guest
        } else {
            Session.Authenticated(
                collegeEmail = email,
                // Falls back to REGISTERED for pre-migration/corrupt stored values, not new business logic.
                role = prefs[ROLE]?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: UserRole.REGISTERED,
                domain = prefs[DOMAIN]?.let { runCatching { UserDomain.valueOf(it) }.getOrNull() }
            )
        }
    }

    override suspend fun currentAccessToken(): String? =
        dataStore.data.first()[ACCESS_TOKEN]?.takeIf { it.isNotBlank() }

    override suspend fun signIn(accessToken: String, collegeEmail: String, role: UserRole, domain: UserDomain?) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[COLLEGE_EMAIL] = collegeEmail
            prefs[ROLE] = role.name
            if (domain != null) prefs[DOMAIN] = domain.name else prefs.remove(DOMAIN)
        }
    }

    override suspend fun updateRoleAndDomain(role: UserRole, domain: UserDomain?) {
        dataStore.edit { prefs ->
            prefs[ROLE] = role.name
            if (domain != null) prefs[DOMAIN] = domain.name else prefs.remove(DOMAIN)
        }
    }

    override suspend fun signOut() {
        dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(COLLEGE_EMAIL)
            prefs.remove(ROLE)
            prefs.remove(DOMAIN)
            // hasSeenIntro deliberately survives a sign-out — the intro is a one-time thing.
        }
    }

    override suspend fun hasSeenIntro(): Boolean =
        dataStore.data.first()[HAS_SEEN_INTRO] == true

    override suspend fun markIntroSeen() {
        dataStore.edit { prefs -> prefs[HAS_SEEN_INTRO] = true }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val COLLEGE_EMAIL = stringPreferencesKey("college_email")
        val ROLE = stringPreferencesKey("role")
        val DOMAIN = stringPreferencesKey("domain")
        val HAS_SEEN_INTRO = booleanPreferencesKey("has_seen_intro")
    }
}
