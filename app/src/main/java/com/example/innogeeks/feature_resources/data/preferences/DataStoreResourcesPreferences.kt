package com.example.innogeeks.feature_resources.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.innogeeks.feature_resources.domain.ResourcesPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// One DataStore instance per process, hung off Context as the DataStore docs require.
private val Context.resourcesDataStore: DataStore<Preferences> by preferencesDataStore(name = "innogeeks_resources")

class DataStoreResourcesPreferences(context: Context) : ResourcesPreferences {

    private val dataStore = context.resourcesDataStore

    override val tipSeen: Flow<Boolean> = dataStore.data.map { it[TIP_SEEN] ?: false }

    override suspend fun markTipSeen() {
        dataStore.edit { it[TIP_SEEN] = true }
    }

    private companion object {
        val TIP_SEEN = booleanPreferencesKey("tip_seen")
    }
}
